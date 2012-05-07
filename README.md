# Walton

**Walton is a suite of tools for guiding exploration and discovery in the Clojure REPL.**

---

## Open Questions

* What will I use for parsing the HTML logs?
* How do I keep those logs up to date?
* Should I mirror chouser's site or log on my own?
* Should I cast all of chouser's logs to a new format that fits the style of my own irc logger?
* Will getclojure.org be the website where examples are housed?
* How can I make my full data set publicly available?
* Are there any concerns about logging #clojure with no notice?
* Are there multiple projects involved here that could be built into a suite?
** I think so. sente is for exploration, gote is for cargo culting by example.
** examples-of => gote
** cheat => sente
** orient-around

## Getting Walton

* Get walton from [clojars](http://clojars.org).
* If you're using [leiningen](linktoleiningen), create a new project by doing `lein new myprojectname`.
* Edit `project.clj` and add `["org.getclojure.walton" "1.0"]`to the `:dev-dependencies`. It should look something like this:

      (defproject …
        :description …
        :dependencies …
        :dev-dependencies ["org.getclojure.walton", "1.0"])

* Save the file and run `lein deps` to pull walton and its dependencies into your project.
* Start up a REPL by typing `lein repl`.

## Walton Overview

Walton has N main components:

### (examples-of …)

    user> (examples-of let)
    => [(let [x 1] x), (let [[x y] [1 2]] {:x x, :y y}), …]

### (cheat …)

    user> (cheat let)
    => "### let ###
        1. destructuring
        2. special keywords and reader macros
        3. clojuredocs user-submitted usage
        4. examples used in the #clojure irc channel"

### (orient-around …)

    user> (orient-around seq)
    => Returns a clojure-atlas ontological representation of seq's place
       in clojure. {:parents […], :siblings […]}


### ClojureFS

#### (ls)

#### (cd)

#### (pwd)

#### (cat)
