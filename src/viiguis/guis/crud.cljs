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
      [:div.form
       [:div.flex-row
        [:div.flex-column.flex-grow
         [:label.crud--input-label.flex-row
          "Surname prefix:"
          ; the spec says this should only match surname prefixes. It would be easy to make this more versatile
          [:input.crud--input {:on-change (fn [ev]
                                            (reset! surname-filter (.. ev -target -value)))}]]
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
                      :size 15}]))]
        [:div.crud--inputs.flex-column
         [:label.crud--input-label
          "Name:"
          [:input.crud--input {:default-value (:name @editing-item)
                               :on-change (fn [ev]
                                            (swap! editing-item assoc :name (.. ev -target -value)))}]]
         [:label.crud--input-label
          "Surname:"
          [:input.crud--input {:default-value (:surname @editing-item)
                               :on-change (fn [ev]
                                            (swap! editing-item assoc :surname (.. ev -target -value)))}]]]]
       [:div.flex-row
        [:button.crud--button
         {:on-click #(swap! db conj @editing-item)
          :disabled (or (string/blank? (:name @editing-item))
                        (string/blank? (:surname @editing-item)))}
         "Create"]
        [:button.crud--button
         {:on-click #(swap! db (fn [db-val]
                                 (-> db-val
                                     (disj (selected-item))
                                     (conj @editing-item))))
          :disabled (or (not (selected-item))
                        (string/blank? (:name @editing-item))
                        (string/blank? (:surname @editing-item)))}
         "Update"]
        [:button.crud--button
         {:on-click #(swap! db disj (selected-item))
          :disabled (not (element-value "#selected-item"))}
         "Delete"]]])))
