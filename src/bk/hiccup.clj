(ns bk.hiccup
  (:require [hiccup.def :as d]
            [hiccup.page :as p]
            [hiccup2.core :as h])
  (:import java.text.SimpleDateFormat))

(defn render-general-ledger [gl]
  (->> [:div
        [:h2 "総勘定元帳"]
        (for [[k v] gl]
          [:details
           [:summary {:style {:cursor "pointer"}} (name k)]
           [:table {:style {:border "solid"
                            :margin-top "3em"
                            :width "50%"}}
            [:thead [:tr [:th "日付"]
                     [:th "摘要"]
                     [:th {:colspan 2} (str (name k) "元帳")]
                     [:th "残高"]]]
            [:tbody
             (for [{:keys [account amount comment balance date krkt?]} v]
               [:tr
                [:td {:style {:border "solid"}} (.format (SimpleDateFormat. "MM/dd") date)]
                [:td {:style {:border "solid"}}
                 [:pre comment]]
                [:td {:style {:border "solid"}} (or (when krkt? amount) "-")]
                [:td {:style {:border "solid"}} (or (when-not krkt? amount) "-")]
                [:td {:style {:border "solid"}} balance]])]]])]))

(d/defhtml tb-table [{:keys [貸方 借方 貸方計 借方計]}]
  [:div
   [:h2 "残高試算表" ;;貸借対照表
    ]
   [:div {:style {:display "inline-block"
                  :border "solid"
                  :height "18em"}}
    [:div {:style {:display "inline-block"
                   :vertical-align "bottom"}}
     [:span 貸方計]]
    [:div {:style {:display "inline-block"
                   :width "10em"
                   :position "relative"
                   :border-right "solid"
                   :height "100%"
                   :border-left "solid"}}
     [:div {:style {:border-bottom "dotted"}} [:strong "貸方"]]
     [:div
      (keep (fn [[k v]]
              (when-not (= v 0)
                (list
                 [:div
                  [:span {:style {:text-align "left"}} k]
                  [:span {:style {:float "right"}} v]]))) 貸方)]]
    [:div {:style {:display "inline-block"
                   :width "10em"
                   :border-right "solid"
                   :height "100%"
                   :vertical-align "top"}}
     [:div {:style {:border-bottom "dotted"}} [:strong "借方"]]
     [:div (keep (fn [[k v]]
                   (when-not (= v 0)
                     (list
                      [:div
                       [:span {:style {:text-align "left"}} k]
                       [:span {:style {:float "right"}} v]]))) 借方)]]
    [:div {:style {:display "inline-block"
                   :vertical-align "bottom"}}
     [:span 借方計]]]]
  [:div])

(d/defhtml pl-table [{:keys [貸方 借方 貸方計 借方計]}]
  [:div
   [:h2 "損益計算書" ;;貸借対照表
    ]
   [:div {:style {:display "inline-block"
                  :border "solid"
                  :height "15em"}}
    [:div {:style {:display "inline-block"
                   :vertical-align "bottom"}}
     [:span 貸方計]]
    [:div {:style {:display "inline-block"
                   :width "10em"
                   :position "relative"
                   :border-right "solid"
                   :height "100%"
                   :border-left "solid"}}
     [:div {:style {:border-bottom "dotted"}} [:strong "貸方"]]
     [:div
      (keep (fn [[k v]]
              (when-not (= v 0)
                (list
                 [:div
                  [:span {:style {:text-align "left"}} k]
                  [:span {:style {:float "right"}} v]]))) 貸方)]]
    [:div {:style {:display "inline-block"
                   :width "10em"
                   :border-right "solid"
                   :height "100%"
                   :vertical-align "top"}}
     [:div {:style {:border-bottom "dotted"}} [:strong "借方"]]
     [:div (keep (fn [[k v]]
                   (when-not (= v 0)
                     (list
                      [:div
                       [:span {:style {:text-align "left"}} k]
                       [:span {:style {:float "right"}} v]]))) 借方)]]
    [:div {:style {:display "inline-block"
                   :vertical-align "bottom"}}
     [:span 借方計]]]]  )

(defn statements [{:keys [profit&loss trial-balance general-ledger]}]
  (-> (list
       [:head
        [:link {:rel "stylesheet" :href "https://unpkg.com/purecss@1.0.0/build/pure-min.css"}]]
       [:body
        (tb-table trial-balance)
        (pl-table profit&loss)
        (render-general-ledger general-ledger)])
      p/html5))
