(ns bk.rules
  (:require [bk.records :as r]
            [clara.rules :as c]
            [clara.rules.accumulators :as acc]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [bk.records Karikata Kasikata LedgerEntry LedgerSource Discount]))

(c/defrule load-ledgers
  [LedgerSource (= ?p path)]
  =>
  (c/insert-all! (-> ?p io/file slurp edn/read-string (->> (map r/map->LedgerEntry)))))

(c/defrule split-entry
  "Catches accounts that do not end with '*' which is a signal that some percentage of the
   amount will be deducted later"
  [LedgerEntry (= ?借方 借方) (= ?貸方 貸方) (= ?amt amount) (= ?cmt comment) (= ?dt date)]
  =>
  (when-not (or (str/ends-with? ?借方 "*") (str/ends-with? ?貸方 "*"))
    (c/insert-all! [(r/->Karikata ?借方 ?amt ?cmt ?dt)
                    (r/->Kasikata ?貸方 ?amt ?cmt ?dt)]))  )

(c/defrule discounted-entry
  "Catches accounts that end with '*' and discounts the amount specified by the matching Discount instance."
  [Discount (= ?acc account) (= ?r rate)]
  [LedgerEntry (= ?借方 借方) (= ?貸方 貸方) (= ?amt amount) (= ?cmt comment) (= ?dt date)]
  =>
  (when (and (or (str/ends-with? ?借方 "*") (str/ends-with? ?貸方 "*"))
             (or (= ?acc ?借方) (= ?acc ?貸方)))
    (c/insert-all! [(r/->Karikata (keyword (str/replace (name ?借方) "*" "")) (long (* ?amt ?r)) ?cmt ?dt)
                    (r/->Kasikata (keyword (str/replace (name ?貸方) "*" "")) (long (* ?amt ?r)) ?cmt ?dt)])    ))

(c/defquery by-account
  [:?account]
  [:or
   [?kskt <- Kasikata (= ?account account)]
   [?krkt <- Karikata (= ?account account)]])

(c/defquery balance
  []
  [?貸方現金 <- (acc/sum :amount) :from [Kasikata (= account :現金)]]
  [?借方現金 <- (acc/sum :amount) :from [Karikata (= account :現金)]]
  [?貸方備品 <- (acc/sum :amount) :from [Kasikata (= account :備品)]]
  [?借方備品 <- (acc/sum :amount) :from [Karikata (= account :備品)]]
  [?交通費 <- (acc/sum :amount) :from [Karikata (= account :交通費)]]
  [?借方売掛金 <- (acc/sum :amount) :from [Karikata (= account :売掛金)]]
  [?貸方売掛金 <- (acc/sum :amount) :from [Kasikata (= account :売掛金)]]
  [?交際費 <- (acc/sum :amount) :from [Karikata (= account :交際費)]]
  [?消耗品費 <- (acc/sum :amount) :from [Karikata (= account :消耗品費)]]
  [?原価償却 <- (acc/sum :amount) :from [Karikata (= account :原価償却)]]
  [?水道光熱費 <- (acc/sum :amount) :from [Karikata (= account :水道光熱費)]]
  [?通信費 <- (acc/sum :amount) :from [Karikata (= account :通信費)]]
  [?事業主借 <- (acc/sum :amount) :from [Kasikata (= account :事業主借)]]
  [?事業主貸 <- (acc/sum :amount) :from [Karikata (= account :事業主貸)]]
  [?源泉徴収 <- (acc/sum :amount) :from [Karikata (= account :源泉徴収)]]
  [?雑費 <- (acc/sum :amount) :from [Karikata (= account :雑費)]]
  [?売上 <- (acc/sum :amount) :from [Kasikata (= account :売上)]])
