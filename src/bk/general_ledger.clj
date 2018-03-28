(ns bk.general-ledger
  (:require [clara.rules :as c]
            [bk.rules :as ru]))

(defmulti general-ledger
  (fn [_ account] account))

(defmethod general-ledger :default
  [sess account]
  (when-let [dataseq (->> (c/query sess ru/by-account :?account account)
                          (map (fn [{:keys [?krkt ?kskt]}]
                                 (or ?krkt ?kskt)))
                          (sort-by :date)
                          seq)]
    (loop [entries []
           [{amount :amount :as head} nxt & rest] dataseq]
      (let [el (-> head
                   (assoc :balance  (+ amount (or (-> entries last :balance) 0))))]
        (if nxt
          (recur (conj entries el) (cons nxt rest))
          {account (conj entries el)})))))

(defmethod general-ledger :現金
  [sess account]
  (when-let [dataseq (->> (c/query sess ru/by-account :?account account)
                          (map (fn [{:keys [?krkt ?kskt]}]
                                 (assoc (or ?krkt ?kskt)
                                        :krkt? (some? ?krkt))))
                          (sort-by :date)
                          seq)]
    (loop [entries []
           [{krkt? :krkt? amount :amount :as head} nxt & rest] dataseq]
      (let [el (-> head
                   (assoc :balance  (+ (if krkt?
                                         amount
                                         (* -1 amount))
                                       (or (-> entries last :balance) 0))))]
        (if nxt
          (recur (conj entries el) (cons nxt rest))
          {account (conj entries el)})))))

(defmethod general-ledger :売掛金
  [sess account]
  (when-let [dataseq (->> (c/query sess ru/by-account :?account account)
                          (map (fn [{:keys [?krkt ?kskt]}]
                                 (assoc (or ?krkt ?kskt)
                                        :krkt? (some? ?krkt))))
                          (sort-by :date)
                          seq)]
    (loop [entries []
           [{krkt? :krkt? amount :amount :as head} nxt & rest] dataseq]
      (let [el (-> head
                   (assoc :balance  (+ (if krkt?
                                         amount
                                         (* -1 amount))
                                       (or (-> entries last :balance) 0))))]
        (if nxt
          (recur (conj entries el) (cons nxt rest))
          {account (conj entries el)})))))
