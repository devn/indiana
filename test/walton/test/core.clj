(ns walton.test.core
  (:use [midje.sweet]))

(def a-z (map char (range 97 123)))
(def A-Z (map char (range 65 91)))
(def a-Z (concat a-z A-Z))

(defn make-rot-seq [as]
  (concat (subvec (vec as) 13 26)
          (subvec (vec as) 0 13)))

(defn rot [s]
  (let [rots (concat (make-rot-seq a-z)
                     (make-rot-seq A-Z))
        rot-map (zipmap a-Z rots)]
    (apply str (map #(get rot-map % " ") s))))

;.;. Not in rewards, but in the strength to strive, the blessing lies. --
;.;. Towbridge
(facts "about rot13"
  (rot "abcdefghijklmnopqrstuvwxyz") => "nopqrstuvwxyzabcdefghijklm"
  (rot "hello world") => "uryyb jbeyq"
  (rot "Devin Walters") => "Qriva Jnygref")

