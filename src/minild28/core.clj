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
(import java.awt.event.KeyListener)
(import javax.swing.JFrame)
(import javax.swing.JPanel)

(def *size* 500)

(def x (ref 3))
(def y (ref 3))
(def keys-down (atom #{}))

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

(defn drawRectangle [p]
  (let [bfs (.getBufferStrategy p)]
    (doto (.getDrawGraphics bfs)  
      (.setColor (java.awt.Color/WHITE))
      (.fillRect 0 0 200 200)
      (.setColor (java.awt.Color/BLUE))
      (.fillRect (* 10 (deref x)) (* 10 (deref y)) 10 10))
    (.show bfs)
    (.sync (Toolkit/getDefaultToolkit))
))

(defn game[]
  (loop []
    (drawRectangle frame)
    (Thread/sleep 5)
    (.setTitle frame "ff")
    ;(.setTitle frame (str @keys-down))
    (recur)))

(defn -main[]
  (future (game)))
