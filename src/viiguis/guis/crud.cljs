(ns viiguis.guis.crud
  (:require
    [reagent.core :as r]
    [clojure.string :as string]
    [cljs.reader]))

; This time we use a global instead of component-local DB
; if we wanted to make this local it would be a matter of simply dropping it into the
; component below, and adding a db parameter to the CRUD fns

(defonce db (r/atom {}))

; CRUD fns

(defn create-item!
  [item]
  ;; cast the uuid to string because it will get mangled by the select value anyways
  (swap! db assoc (str (random-uuid)) item))

(defn list-items
  [surname-filter]
  (->> @db
       (map (fn [[id item]]
              (assoc item :id id)))
       (filter (fn [{:keys [surname]}]
                 (or (not surname-filter)
                     (string/starts-with? surname surname-filter))))
       (sort-by (juxt :surname :name))))

(defn read-item
  [item-id]
  (get @db item-id))

(defn update-item!
  [item-id new-vals]
  (swap! db update item-id merge new-vals))

(defn delete-item!
  [item-id]
  (swap! db dissoc item-id))

; The UI

(defn item-selector
  [{:keys [value on-change]}]
  (let [surname-filter (r/atom nil)]
    (fn [_]
      [:div.flex-column.flex-grow
       [:label.crud--input-label.flex-row
        "Surname prefix:"
        ; the spec says this should only match surname prefixes. It would be easy to make this more versatile
        [:input.crud--input
         {:on-change (fn [ev]
                       (reset! surname-filter (.. ev -target -value)))}]]
       [:select
        {:value value
         :on-change (fn [ev]
                      (on-change (.. ev -target -value)))
         :size 15}
        (for [{:keys [id surname name]} (list-items @surname-filter)]
          [:option {:value id}
           (str surname ", " name)])]])))

(defn panel
  []
  (let [editing-item (r/atom nil)
        selected-item-id (r/atom nil)]
    (fn []
      [:div.form
       [:div.flex-row
        ^{:key @selected-item-id}
        [item-selector {:value @selected-item-id
                        :on-change (fn [item-id]
                                     (reset! selected-item-id item-id)
                                     (reset! editing-item (read-item item-id)))}]
        [:div.crud--inputs.flex-column
         [:label.crud--input-label
          "Name:"
          ^{:key @selected-item-id}
          [:input.crud--input {:default-value (:name @editing-item)
                               :on-change (fn [ev]
                                            (swap! editing-item assoc :name (.. ev -target -value)))}]]
         [:label.crud--input-label
          "Surname:"
          ^{:key @selected-item-id}
          [:input.crud--input {:default-value (:surname @editing-item)
                               :on-change (fn [ev]
                                            (swap! editing-item assoc :surname (.. ev -target -value)))}]]]]
       [:div.flex-row
        [:button.crud--button
         {:on-click #(do (create-item! @editing-item))
          :disabled (or (string/blank? (:name @editing-item))
                        (string/blank? (:surname @editing-item)))}
         "Create"]
        [:button.crud--button
         {:on-click #(update-item! @selected-item-id @editing-item)
          :disabled (or (not @selected-item-id)
                        (string/blank? (:name @editing-item))
                        (string/blank? (:surname @editing-item)))}
         "Update"]
        [:button.crud--button
         {:on-click #(do (delete-item! @selected-item-id)
                         (reset! selected-item-id nil))
          :disabled (not @selected-item-id)}
         "Delete"]]])))

(defonce demo
  ; add some demo data
  (when true
    (create-item! {:surname "Mustermann" :name "Max"})
    (create-item! {:surname "Mustermann" :name "Erika"})))