(ns viiguis.guis.cells-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [viiguis.guis.cells :as cells]))

(deftest parse-formula-test
  (is (= 1.2 (cells/parse-formula "1.2")))
  (is (= "foo" (cells/parse-formula "foo")))
  (is (= 1 (cells/parse-formula "=1")))
  (is (= [(:sum cells/operators) 1 2]
         (cells/parse-formula "=(sum 1 2)")))
  (is (= [(:sum cells/operators) [(:div cells/operators) 1 2] -2 3]
         (cells/parse-formula "=(SUM(DIV 1 2) -2 3)")))
  (is (= [:ref [0 1]]
         (cells/parse-formula "=A1")))
  (is (= [(:sum cells/operators) [:ref [0 1]] [:ref [1 2]]]
         (cells/parse-formula "=(SUM A1 B2)")))
  (is (= [(:mul cells/operators) [:ref [0 1] [0 2] [1 1] [1 2]] [:ref [2 99]]]
         (cells/parse-formula "=(MUL A1:B2 C99)"))))

(deftest eval-formula-test
  (is (= 1.2 (cells/eval-formula nil (cells/parse-formula "1.2"))))
  (is (= "foo" (cells/eval-formula nil (cells/parse-formula "foo"))))
  (is (= 1 (cells/eval-formula nil (cells/parse-formula "=1"))))
  (is (= 3 (cells/eval-formula nil (cells/parse-formula "=(sum 1 2)"))))
  (is (= 1.5 (cells/eval-formula nil (cells/parse-formula "=(SUM(DIV 1 2) -2 3)"))))
  (let [sheet (cells/make-sheet 10 20)]
    (cells/set-cell! sheet (cells/reference->key "A0") 1)
    (cells/set-cell! sheet (cells/reference->key "A1") -1)
    (cells/set-cell! sheet (cells/reference->key "B2") 2)
    (cells/set-cell! sheet (cells/reference->key "C19") -11)
    (is (= 1 (cells/eval-formula sheet (cells/parse-formula "=A0"))))
    (is (= 3 (cells/eval-formula sheet (cells/parse-formula "=(SUM A0 B2)"))))
    (is (= 0.2 (cells/eval-formula sheet (cells/parse-formula "=(DIV A0 5)"))))
    (is (= 22 (cells/eval-formula sheet (cells/parse-formula "=(MUL A0:B2 C19)"))))))

(deftest formula-cells-test
  (is (= [] (cells/formula-references* "foo")))
  (is (= [] (cells/formula-references* "1")))
  (is (= [] (cells/formula-references* "=1")))
  (is (= [[0 2]] (cells/formula-references* "=A2")))
  (is (= [[1 1] [1 2] [1 3]] (cells/formula-references* "=(MUL (SUM B1 B2) B3 15)")))
  (is (= [[0 1] [1 2] [2 2] [2 3]] (cells/formula-references* "=(AVG(SUM A1 B2) C2 C3)"))))