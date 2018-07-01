(define (eval-body body)
  (if (null? body) #f
    (let ((first (car body))
          (rest (cdr body)))
      (let ((condition (car first))
            (post (cdr first)))
        (if (or (null? rest) (condition))
          (post)
          (eval-body body))))))

(define (is-Int? a)
  (number? a))

(define (is-Str a)
  (string? a))

(define (is-Lambda a)
  (lambda? a))


