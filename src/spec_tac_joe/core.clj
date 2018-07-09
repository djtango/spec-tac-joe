(ns spec-tac-joe.core
  (:require [clojure.spec.alpha :as s]
            [clojure.set :as set]
            [clojure.spec.gen.alpha :as gen]))

;; RULES OF TIC TAC TOE
;; There is a 3x3 board
;; There are two pieces: x and o
;; Turns alternate between o and x
;; O goes first
;; Three pieces in a line wins:
;; i.e. horizontally, vertically and diagonally

(s/def ::position (s/int-in 0 9))
(s/def ::move ::position)
(s/def ::X (s/coll-of ::position))
(s/def ::O (s/coll-of ::position))


(defn length-of-inputs-equal-to-length-of-output?[{:keys [args ret]}]
  (let [{:keys [O X]} args
        output ret]
    (= (+ (count O)
          (count X))
       (count output))))
(s/fdef interleave*
        :args (s/cat :O ::O
                     :X ::X)
        :ret (s/coll-of ::position)
        :fn length-of-inputs-equal-to-length-of-output?)
(defn interleave* [O X]
  (let [new-turns (interleave O X)
        countO (count O)
        countX (count X)
        rest-of-longer-coll
        (if (< countO countX)
          (drop countO X)
          (drop countX O))]
    (into []
          (concat new-turns
                  rest-of-longer-coll))))

(defn interleaving-O-X-returns-same-turns? [{:keys [ret args]}]
  (let [turns (:turns args)
        {:keys [O X]} ret]
    (= (interleave* O X)
       turns)))

(s/fdef split-by-player
        :args (s/cat :turns (s/coll-of ::position))
        :ret (s/keys :req-un [::X ::O])
        :fn interleaving-O-X-returns-same-turns?)
(defn split-by-player [turns]
  (let [take-when
        (fn [odd-or-even]
          (->> turns
               (map-indexed (fn [idx itm] [idx itm]))
               (filter (comp odd-or-even first))
               (mapv second)))]
    {:X (take-when odd?)
     :O (take-when even?)}))


(s/def ::turns
  (s/conformer (fn [turns]
                 (if (s/valid? (s/coll-of ::move :max-count 9) turns)
                   (split-by-player turns)
                   ::s/invalid))
               (fn [{:keys [X O]}]
                 (interleave O X))))


;; win?
(def first-row  #{0 1 2})
(def second-row #{3 4 5})
(def third-row  #{6 7 8})
(def first-col  #{0 3 6})
(def second-col #{1 4 7})
(def third-col  #{2 5 8})
(def back-slash    #{0 4 8})
(def forward-slash #{2 4 6})

(s/def ::first-row (partial set/subset? first-row))
(s/def ::second-row (partial set/subset? second-row))
(s/def ::third-row  (partial set/subset? third-row))
(s/def ::first-col  (partial set/subset? first-col))
(s/def ::second-col (partial set/subset? second-col))
(s/def ::third-col  (partial set/subset? third-col))

(s/def ::back-slash    (partial set/subset? back-slash))
(s/def ::forward-slash (partial set/subset? forward-slash))

(s/def ::winning-state (s/or :h1 ::first-row
                             :h2 ::second-row
                             :h3 ::third-row
                             :v1 ::first-col
                             :v2 ::second-col
                             :v3 ::third-col
                             :d1 ::back-slash
                             :d2 ::forward-slash))

(defn win? [turns]
  (let [{:keys [O X]} (s/conform ::turns turns)
        valid? (partial not= ::s/invalid)]
    (or (valid? (s/conform ::winning-state O))
        (valid? (s/conform ::winning-state X)))))

(s/fdef play
        :args (s/cat :turns (s/and (s/coll-of ::move :max-count 9)
                                   ::turns)
                     :move ::position)
        :ret (s/or :end #{:win :draw}
                   :continue ::turns)
        :fn (fn [{:keys [args]}]
              (let [turns (set (:turns args))
                    move  (:move args)]
                (empty? (turns move)))))
(defn play [turns move]
  (let [next-move (conj turns move)]
    (cond (win? next-move)
          :win

          (>= (count next-move) 9)
          :draw

          :else
          next-move)))

(defn current-player [turns]
  (if (even? (count turns)) :O :X))

(defn empty-position? [{:keys [ret args]}]
  (let [turns (set (:turns args))]
    (empty? (turns ret))))
(s/fdef get-input!
        :args (s/cat :turns ::turns)
        :ret ::position
        :fn empty-position?)
(defn get-input! [turns]
  (let [player (current-player turns)]
    (do (print (str "Place your next " (name player) ":"))
       (flush)
       (Integer/parseInt (read-line)))))

(defn terminate [turns move]
  (let [player (current-player turns)
        next-state (play turns move)]
    (if (= :win next-state)
      (println (str "Game Over: " (name player) " wins!"))
      (println "Draw."))))

(defn game-loop []
  (loop [turns []
         move (get-input! turns)]
    (let [next-state (play turns move)]
      (if (#{:win :draw} next-state)
        (terminate turns move)
        (recur next-state (get-input! next-state))))))


;; Playing with regex patterns to re-implement the linked list
;; (s/def ::X (s/cat :X ::move :next (s/* ::O)))
;; (s/def ::O (s/cat :O ::move :next (s/* ::X)))
;; (s/def ::alternating-moves (s/cat :X ::move :O ::move))
;; (s/def ::turns (s/+ (s/cat :O ::move :rest (s/+ ::alternating-moves))))
;; (s/def ::turns (s/and (s/coll-of ::position :max-count 9)
;;                       ::O))
;;(s/def ::O (s/cat :O ::move :next (s/* ::X)))



;; Playing with generators
;; (s/def ::turns
;;   (s/with-gen
;;     (s/conformer (fn [turns]
;;                    (if (s/valid? (s/coll-of ::move :max-count 9) turns)
;;                      (split-by-player turns)
;;                      ::s/invalid))
;;                  (fn [{:keys [X O]}]
;;                    (interleave O X)))
;;     (fn []
;;       (let [state (atom #{})]
;;         (gen/such-that (fn [g]
;;                          (let [allowed? (nil? (@state g))]
;;                            (if allowed?
;;                              (swap! state conj g)
;;                              false)))
;;                        (gen/choose 0 8)
;;                        1000000)))))

