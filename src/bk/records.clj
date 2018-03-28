(ns bk.records)

(defrecord Discount [account rate])
(defrecord LedgerSource [path])
(defrecord LedgerEntry [借方 貸方 amount comment date])
(defrecord Karikata [account amount comment date])
(defrecord Kasikata [account amount comment date])
