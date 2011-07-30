(ns minild28.core
  (:import
    (java.awt Color Dimension)
    (java.awt.event KeyListener)
    (java.awt.image BufferStrategy)
    (javax.swing JFrame JOptionPane JPanel)))

(import java.awt.Dimension)
(import java.awt.Toolkit)
(import java.awt.event.KeyListener)
(import javax.swing.JPanel)
(import javax.swing.JFrame)

(import java.awt.Color)
(import java.awt.Dimension)

(def *size* 500)
(def *t-width* 20)

(def keys-down (atom #{}))

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

(defn draw-tile [x-rel y-rel type gfx]
  (let [y-offs 25 x-offs 10]
    (.setColor gfx (cond (= type \1) (java.awt.Color/BLUE)
                         (= type \0) (java.awt.Color/GREEN)
                         (= type \c) (java.awt.Color/BLACK)))

    (.fillRect gfx (+ x-offs (* x-rel *t-width*))
                   (+ y-offs (* y-rel *t-width*))
                   *t-width* *t-width*)))

(defn render-map [gfx]
  (doseq [x (range (count map1))]
    (doseq [y (range (count (first map1)))]
      (let [ch (nth (nth map1 y) x)]
        (draw-tile x y ch gfx)))))

(defn render-game [gfx player]
  (.setColor gfx (java.awt.Color/BLACK))

  (.fillRect gfx 0 0 *size* *size*)
  (render-map gfx)

  (.setColor gfx (java.awt.Color/WHITE))
  (draw-tile (:x player) (:y player) \c gfx)

  ;(.setColor (java.awt.Color/BLUE))
  ;(.fillRect (* 10 (deref x)) (* 10 (deref y)) 10 10)
)

(defn drawRectangle [p player]
  (let [bfs (.getBufferStrategy p)
        gfx (.getDrawGraphics bfs)]
    (render-game gfx player)
    (.show bfs)
    (.sync (Toolkit/getDefaultToolkit))
))

(defn update-player [player]
  (let [old-x (:x player)
        old-y (:y player)
        
        new-x 2
        ]
    player))

(defn game [frame]
  (loop [player {:x 3 :y 3}]
    (drawRectangle frame player)
    (Thread/sleep 5)
    (.setTitle frame "WAR.")
    (recur (update-player player))
    ))

(defn -main[]
  (def panel
    (proxy [JPanel KeyListener] []
      (getPreferredSize [] (Dimension. *size* *size*))
      (keyPressed [e]
        (let [key-code (.getKeyCode e)]
          (swap! keys-down conj key-code)))
         
      (keyReleased [e]
        (let [key-code (.getKeyCode e)]
          (swap! keys-down disj key-code)))

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

  (future (game frame)))

(-main)
