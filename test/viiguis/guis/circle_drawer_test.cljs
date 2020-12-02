(ns viiguis.guis.circle-drawer-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [viiguis.guis.circle-drawer :as gui]))

(deftest conj-edit-test
  (is (= {:edit-stack '([1 1 20] [0 0 10])
          :redo-stack '()}
         (gui/conj-edit
           {:edit-stack '([0 0 10])
            :redo-stack '([2 2 10])}
           [1 1 20]))))

(deftest undo-test
  (is (= {:selected-circle [1 1 20]
          :edit-stack '([1 1 20])
          :redo-stack '([0 0 10])}
         (gui/undo {:selected-circle [1 1 20]
                    :edit-stack '([0 0 10] [1 1 20])
                    :redo-stack '()})))
  (is (= {:selected-circle nil
          :edit-stack '([1 1 20])
          :redo-stack '([0 0 10])}
         (gui/undo {:selected-circle [0 0 10]
                    :edit-stack '([0 0 10] [1 1 20])
                    :redo-stack '()}))))

(deftest redo-test
  (is (= {:edit-stack '([0 0 10] [1 1 20])
          :redo-stack '()}
         (gui/redo {:edit-stack '([1 1 20])
                    :redo-stack '([0 0 10])}))))

(deftest circle-geometry-test
  (is (= '()
         (gui/circle-geometry {:edit-stack '()})))
  (is (= '([0 0 10])
         (gui/circle-geometry {:edit-stack '([0 0 10])})))
  (is (= '([1 1 10]
           [0 0 10])
         (gui/circle-geometry {:edit-stack '([1 1 10] [0 0 10])})))
  (is (= '([0 0 20] [1 1 10])
         (gui/circle-geometry {:edit-stack '([0 0 20] [1 1 10] [0 0 10])})))
  (is (= '([0 0 20] [1 1 10])
         (gui/circle-geometry {:current-edit [0 0 20]
                               :edit-stack '([1 1 10] [0 0 10])}))))
