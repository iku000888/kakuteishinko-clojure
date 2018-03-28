(ns bk.core
  (:require [bk.records :as r]
            [bk.rules :as ru]
            [bk.general-ledger :as gl]
            [clojure.pprint :as pp]
            [clara.rules :as c])
  (:import [bk.records Karikata Kasikata LedgerEntry LedgerSource]))

(def accounts #{:現金 :売掛金 :交通費 :交際費 :消耗品費 :水道光熱費 :通信費 :雑費 :売上 :事業主借 :事業主貸 :源泉徴収 :備品 :原価償却})

(def nominal-accounts #{:売上 :消耗品費 :交通費 :交際費 :水道光熱費 :通信費 :雑費 :原価償却})

;;Trial balance
(defn trial-balance [{:keys [?貸方現金 ?借方現金 ?借方売掛金 ?貸方売掛金 ?交通費
                             ?交際費 ?消耗品費 ?水道光熱費 ?通信費 ?売上 ?雑費
                             ?貸方備品 ?借方備品 ?原価償却 ?事業主貸 ?事業主借 ?源泉徴収]}]
  (let [cash (- ?借方現金 ?貸方現金)
        receivable (- ?借方売掛金 ?貸方売掛金)
        krkt-accounts [[:現金 cash] [:交通費 ?交通費] [:交際費 ?交際費]
                       [:消耗品費 ?消耗品費] ;;[:売掛金 ?借方売掛金]
                       [:水道光熱費 ?水道光熱費] [:通信費 ?通信費] [:雑費 ?雑費]
                       [:事業主貸 ?事業主貸]
                       [:売掛金 receivable] [:原価償却 ?原価償却]
                       [:源泉徴収 ?源泉徴収]]
        kskt-accounts [[:売上 ?売上] [:事業主借 ?事業主借] [:備品 (- ?借方備品 ?貸方備品)]]]
    {:借方 krkt-accounts :貸方 kskt-accounts
     :貸方計 (->> kskt-accounts (map second) (reduce +))
     :借方計 (->> krkt-accounts (map second) (reduce +))}))

;;Profit&Loss
(defn profit&loss [{:keys [?貸方現金 ?借方現金 ?借方売掛金 ?貸方売掛金 ?交際費
                           ?交通費 ?消耗品費 ?水道光熱費 ?通信費 ?雑費 ?売上
                           ?事業主貸 ?事業主借 ?源泉徴収]}]
  (let [cash (- ?借方現金 ?貸方現金)
        krkt-accounts [[:現金 cash]
                       [:交通費 ?交通費]
                       [:交際費 ?交際費]
                       [:売掛金 ?借方売掛金]
                       [:消耗品費 ?消耗品費]
                       [:水道光熱費 ?水道光熱費]
                       [:通信費 ?通信費]
                       [:雑費 ?雑費]
                       [:事業主貸 ?事業主貸]
                       [:源泉徴収 ?源泉徴収]]
        kskt-accounts [[:売上 ?売上]
                       [:事業主借 ?事業主借]
                       [:売掛金 ?貸方売掛金]]
        krkt-accounts-nominals (filter (comp nominal-accounts first) krkt-accounts)
        kskt-accounts-nominals (filter (comp nominal-accounts first) kskt-accounts)
        krkt-total (->> krkt-accounts-nominals (map second) (reduce +))
        kskt-total (->> kskt-accounts-nominals (map second) (reduce +))
        profit-amt (- kskt-total krkt-total)]
    {:借方 (conj krkt-accounts-nominals
                 [:!*当期利益 profit-amt])
     :貸方 kskt-accounts-nominals
     :貸方計 kskt-total
     :借方計 (+ krkt-total profit-amt)}))

(c/defsession bk-rules 'bk.rules)

(defn financial-statement [sess]
  (let [[balance] (c/query sess ru/balance)]
    (prn balance)
    {:profit&loss (profit&loss balance)
     :trial-balance (trial-balance balance)
     :general-ledger (->> accounts
                          (map #(gl/general-ledger sess %))
                          (into {}))}))
