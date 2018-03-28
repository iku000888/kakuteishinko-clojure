(set-env!
 :resource-paths #{"src"}
 :dependencies '[[com.cerner/clara-rules "0.16.0"]
                 [hiccup "2.0.0-alpha1"]
                 [org.clojure/data.csv "0.1.4"]])

(require '[bk.core :as core])
(require '[bk.hiccup :as h])
(require '[bk.records :as r])
(require '[clara.rules :as c])
(require '[clojure.data.csv :as csv])
(require '[clojure.java.io :as io])

(def tax-withholding-rate 0.1021)
(def consumer-tax-rate 0.08)

(deftask financial-statement []
  (-> core/bk-rules
      (c/insert (r/->LedgerSource "expenses.edn"))
      (c/insert (r/->LedgerSource "uriage.edn"))
      c/fire-rules
      core/financial-statement
      h/statements
      (->> (spit "financial-statement.html"))))
