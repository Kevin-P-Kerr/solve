(define (make-entity) 
  (cons '() (make-strong-eq-hash-table 100)))

(define (set-literal! entity lit) 
  (set-car! entity lit))

(define (evaluate-body body)
    (if (null? body) #f
        (construct-relation body)))


(define (construct-relation lol)
    (if (null? lol) #f
        (let ((l (car lol))
            (rest (cdr lol)))
            (if (and (l) (all-false? rest))
              
(define (relation? ))
