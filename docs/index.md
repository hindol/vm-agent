# VM Agent

## Clojure in 15 Minutes

### Hello, Clojure!

```clojure
(println "Hello, world!")
```

- The same in Python,
    ```python
    print('Hello, world!')
    ```
- `(` comes before the function, not after.
- `(x y z)` => Call function `x` with two arguments, `y` and `z`.

### Arithmetic

```clojure
(+ 1 2 3 4 5)  ;; => 15
(- 10 4 3 2)   ;; => 1
(- 1)          ;; => -1
(* 1 2 3 4 5)  ;; => 120
(/ 6 3 2)      ;; => 1
(/ 10)         ;; => 1/10
(float (/ 10)) ;; => 0.1
```

- Arithmatic operators in Clojure support more than two operands.
- `(f (g x))` => Call function `g` with argument `x`. Then call function `f` with the result of the previous function as argument.

### Lists & Sets

```clojure
[1 "two" 3.0]         ;; => A list
(nth [1 "two" 3.0] 2) ;; => 3.0

#{1 "two" 3.0}                 ;; => A set
(contains? #{1 "two" 3.0} 2.0) ;; false
(#{1 "two" 3.0} 2.0)           ;; false
```

- Collections in Clojure are not *typed*. Much like Python.
- Functions that return `true/false` usually end in a `?`. However, this is not enforced.
- A set is also a function(!)

### Maps

```clojure
{:one 1 :two 2.0 :three "three"}        ;; A map
(:one {:one 1 :two 2.0 :three "three"}) ;; => 1
```

- Symbols starting with `:` are keywords. Think `enums` or named strings. Keywords are primarily used as map keys.
- Keywords are also functions(!)

### Symbols

```clojure
(def x 10)
```

- `x` is a symbol.
- `10` is a value.
- `def` *binds* a symbol to a value.

### Functions

```clojure
(fn [x] (* x x))     ;; A function without a name
((fn [x] (* x x)) 2) ;; => 4
```

- `((fn [x] (* x x)) 2)` => Call function `(fn [x] (* x x))` with the argument `2`.

### Give that Function a Name

```clojure
(def square (fn [x] (* x x))) ;; Bind the symbol "square" to a function
(square 2)                    ;; => 4
```

### The Essence of a Functional Language, Pure Functions