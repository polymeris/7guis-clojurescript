(ns viiguis.guis.crud
  (:require
    [reagent.core :as r]
    [clojure.string :as string]
    [cljs.reader]))

; Obviously this will remove duplicates
; Even "merge" records if we update the names to be the same
; alternatively we could have a vector of items or even a map, where we key items by an id (e.g UUID)
(defonce db (r/atom #{{:surname "Mustermann" :name "Max"}
                      {:surname "Mustermann" :name "Erika"}}))

(defn- element-value
  [selector]
  (when-let [element (js/document.querySelector selector)]
    (.-value element)))

; it's would be cleaner to keep the selected item as explicit state instead of hidden in the value of this element
; ... maybe later, this works for now
(defn- selected-item
  []
  (some-> (element-value "#selected-item") cljs.reader/read-string))

(defn panel
  []
  (let [surname-filter (r/atom nil)
        editing-item (r/atom nil)]
    (fn []
      [:div {:style {:display :flex
                     :flex-direction :column}}
       [:div {:style {:display :flex
                      :flex-direction :row
                      :flex-wrap :wrap}}
        [:div {:style {:display :flex
                       :flex-grow 1
                       :flex-direction :column}}
         [:label {:style {:display :inline-flex
                          :justify-content :space-between
                          :align-items :baseline}}
          "Surname prefix:"
          ; the spec says this should only match surname prefixes. It would be easy to make this more versatile
          [:input {:on-change (fn [ev]
                                (reset! surname-filter (.. ev -target -value)))
                   :style {:margin-left "1em"
                           :flex-grow 1}}]]
         (->> @db
              (filter (fn [{:keys [surname]}]
                        (or (not @surname-filter)
                            (string/starts-with? surname @surname-filter))))
              (sort-by (juxt :surname :name))
              (map (fn [{:keys [surname name] :as item}]
                     [:option {:value (pr-str item)}
                      (str surname ", " name)]))
              (into [:select#selected-item
                     {:on-change (fn [ev]
                                   (let [value (cljs.reader/read-string (.. ev -target -value))]
                                     (reset! editing-item value)))
                      :size 15
                      :style {:margin "1em 0"}}]))]
        [:div {:style {:display :flex
                       :flex-direction :column
                       :justify-content :flex-end
                       :align-items :flex-end
                       :margin "1em 0 1em 1em"}}
         [:label {:style {:margin-bottom "1em"}}
          "Name:"
          [:input {:default-value (:name @editing-item)
                   :on-change (fn [ev]
                                (swap! editing-item assoc :name (.. ev -target -value)))
                   :style {:margin-left "0.5em"}}]]
         [:label
          "Surname:"
          [:input {:default-value (:surname @editing-item)
                   :on-change (fn [ev]
                                (swap! editing-item assoc :surname (.. ev -target -value)))
                   :style {:margin-left "0.5em"}}]]]]
       [:div {:style {:display :flex
                      :flex-direction :row
                      :justify-content :space-between}}
        [:button
         {:on-click #(swap! db conj @editing-item)
          :disabled (or (string/blank? (:name @editing-item))
                        (string/blank? (:surname @editing-item)))
          :style {:flex-grow 1}}
         "Create"]
        [:button
         {:on-click #(swap! db (fn [db-val]
                                 (-> db-val
                                     (disj (selected-item))
                                     (conj @editing-item))))
          :disabled (or (not (selected-item))
                        (string/blank? (:name @editing-item))
                        (string/blank? (:surname @editing-item)))
          :style {:flex-grow 1
                  :margin "0 1em"}}
         "Update"]
        [:button
         {:on-click #(swap! db disj (selected-item))
          :disabled (not (element-value "#selected-item"))
          :style {:flex-grow 1}}
         "Delete"]]])))
