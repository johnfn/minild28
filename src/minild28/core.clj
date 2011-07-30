(ns minild28.core
  (:import
    (java.awt Color Dimension)
    (java.awt.event KeyListener)
    (java.awt.image BufferStrategy)
    (javax.swing JFrame JOptionPane JPanel)))

(import java.awt.Dimension)
(import java.awt.Font)
(import java.awt.Toolkit)
(import java.awt.event.KeyListener)
(import javax.swing.JPanel)
(import javax.swing.JFrame)

(import java.awt.Color)
(import java.awt.Dimension)

(def *size* 700)
(def *t-width* 20)
(def output (atom ["" "" "" "" ""]))

(def keys-down (atom #{}))
(def keys-up (atom #{})) ;for things that only happen when you release a key

;(defn map-generator [size]
;  (defn river-generator [size]
;    (defn helper [coll]
;      (let [[old-x old-y] (last coll)]
;        (if (or (= old-x 0) (= old-y 0) 
;                (= old-x (- size 1))
;                (= old-y (- size 1)))
;          nil
;
;          next-sq 
;    (let [start [(rand-int size) 0]]

(defn log [something]
  (reset! output (subvec (conj @output (str something)) 1 6)))

(defmacro dbg[x] `(let [x# ~x] (log "dbg:" '~x "=" x#) x#))

(def map1
  [
    "01000000000000000000"
    "01000000000000000000"
    "00100000000000000000"
    "00010000000000000000"
    "00010000000000000000"
    "00001000000000000000"
    "00001000000000000000"
    "00001100000000000000"
    "00000100000001110000"
    "00000010001110000000"
    "00000001110000000000"
    "00000000100000000000"
    "00000000010000000000"
    "00000000010000000000"
    "00000000001000000000"
    "00000000001000000000"
    "00000000001100000000"
    "00000000000010000000"
    "00000000000000000000"
    "00000000000000000000" 
    ])

(def *map-size* (count map1))

(def dark-green (java.awt.Color. 60 140 60))
(def light-green (java.awt.Color. 120 190 120))

(defn draw-tile [x-rel y-rel type gfx]
  (let [y-offs 25 x-offs 10]
    (.setColor gfx (cond (= type \1) (java.awt.Color/BLUE)
                         (= type \0) dark-green
                         (= type \h) light-green
                         (= type \c) (java.awt.Color/BLACK)
                         (= type \b) (java.awt.Color/RED)
                         (= type \S) (java.awt.Color/WHITE)
                         ))

    (.fillRect gfx (+ x-offs (* x-rel *t-width*))
                   (+ y-offs (* y-rel *t-width*))
                   *t-width* *t-width*)))

(defn render-map [gfx]
  (doseq [x (range (count map1))]
    (doseq [y (range (count (first map1)))]
      (let [ch (nth (nth map1 y) x)]
        (draw-tile x y ch gfx)))))

(defn join-non-nil [& args]
  "Given args, merges all of them into a list, first removing any nils"
  (filter #(not (nil? %)) args))

(defn neighbors [tile]
  (let [[t-x t-y] tile]
     (join-non-nil
       (if (> t-x 0) [(- t-x 1) t-y] nil)
       (if (> t-y 0) [t-x (- t-y 1)] nil)
       (if (< t-x (- *map-size* 1)) [(+ t-x 1) t-y] nil)
       (if (< t-y (- *map-size* 1)) [t-x (+ t-y 1)] nil)
     )))

(defn to-vect [x]
  "List to vector."
  (apply vector x))

(defn draw-walk-radius [guy gfx]
  (defn get-walkable-tiles [tiles dist-left]
    (if (= dist-left 0)
      tiles
      (let [tiles-new (get-walkable-tiles tiles (- dist-left 1))
            neighbors-new (apply concat (map neighbors tiles-new))
            all-tiles (concat neighbors-new tiles-new)]
        (to-vect all-tiles))))

  (doseq [tile (get-walkable-tiles [[(:x guy) (:y guy)]] 3)]
    (do 
      (draw-tile (first tile) (second tile) \h gfx)))
)

(defn render-state [gfx state]
  ; your guys
  (doseq [guy (:guys state)]
    (do
      (.setColor gfx (java.awt.Color/BLACK))
      (draw-tile (:x guy) (:y guy) \c gfx)))

  ; highlight selected guy
  (if (= (:turn state) :yours)
    ;uses the ([1 2] 0) way of accessing elems
    (let [selected-guy ((:guys state) (:selection state))] 
      (draw-tile (:x selected-guy) (:y selected-guy) \S gfx)
      (draw-walk-radius selected-guy gfx) 
      ))

  ; bad guys
  (doseq [guy (:badguys state)]
    (do
      (.setColor gfx (java.awt.Color/BLACK))
      (draw-tile (:x guy) (:y guy) \b gfx)))
  )

(defn render-game [gfx state]
  (.setColor gfx (java.awt.Color/BLACK))

  (.fillRect gfx 0 0 *size* *size*)

  (render-map gfx)
  (render-state gfx state)

  (let [font (Font. "Serif" Font/PLAIN, 14)]
    (.setColor gfx (java.awt.Color/WHITE))
    (.setFont gfx font)
    (.drawString gfx (str @output) 10 480)
    (.drawString gfx (:message state) 10 460)
  ))

(defn double-buffer-render [p state]
  (let [bfs (.getBufferStrategy p)
        gfx (.getDrawGraphics bfs)]
    (render-game gfx state)
    (.show bfs)
    (.sync (Toolkit/getDefaultToolkit))
))


(defn key-down? [x]
  (if (@keys-down x) 1 0))

;(defn update-player [player]
;  (let [old-x (:x player)
;        old-y (:y player)
;        new-x (+ old-x (key-down? 68) (- (key-down? 65)))
;        new-y (+ old-y (key-down? 83) (- (key-down? 87)))]
;    {:x new-x :y new-y}))

(defn key-up? [key]
  (let [was-in? (@keys-up key)]
    (swap! keys-up disj key)
    was-in?))

(defn update-game-state [state keys-down]
  ; keys-down are the keys that are currently being held down.
  ; keys-up are the keys that were just released.
  (defn update-turn [turn keys-down]
    turn)

  (defn update-guys [guys keys-down]
    guys)

  (defn update-selection [selection keys-down]
    (let [up? (key-up? 78)]
      (if up?
        (- 1 selection)
        selection)))

  (defn update-message [old-message]
    old-message)

  {:guys (update-guys (:guys state) keys-down)
   :badguys (update-guys (:badguys state) keys-down)
   :selection (update-selection (:selection state) keys-down)
   :turn (update-turn (:turn state) keys-down)
   :message (update-message (:message state))
   :tick (+ (:tick state) 1)
  })

; Core game loop
(defn game [frame]
         ; This is the starting state of the entire game.
         ; At first I thought it was weird, but then I realized I kinda like 
         ; how Clojure almost requires you to group all state together...
  (loop [state { :guys    [{:type :guy :x 18  :y 18}
                           {:type :guy :x 16  :y 18}]
                 :badguys [{:type :guy :x 1 :y 2}]
                 :selection 0 ;id of :guys that is selected.
                 :turn :yours
                 :message "N to switch selection. Arrow keys to choose where to walk, Enter to go there."
                 :tick 0
                }]
    (reset! keys-up #{})
    (double-buffer-render frame state)
    (Thread/sleep 15)
    (.setTitle frame (str @keys-up))
    (recur (update-game-state state @keys-down))))

(defn -main[]
  (def panel
    (proxy [JPanel KeyListener] []
      (getPreferredSize [] (Dimension. *size* *size*))
      (keyPressed [e]
        (let [key-code (.getKeyCode e)]
          (swap! keys-down conj key-code)))
         
      (keyReleased [e]
        (let [key-code (.getKeyCode e)]
          (swap! keys-down disj key-code)
          (swap! keys-up conj key-code)
          ))

      (keyTyped [e])))

  (doto panel
    (.setFocusable true)
    (.addKeyListener panel))

  (def frame (JFrame. "Test"))

  (doto frame
      (.add panel)
      (.pack)
      ; (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE) ; Argh! So frustrating.
      ; Why does this line not work?
      (.createBufferStrategy 2)
      (.setVisible true))
  
  (def f (future-call (bound-fn [] (game frame))))
  
 ) 

(-main)
