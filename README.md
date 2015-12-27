# Tetris Devcards experiment

Just playing with [Devcards](https://github.com/bhauman/devcards).

## Demo

![Demo](https://raw.githubusercontent.com/maio/tetris/master/resources/demo.gif)

## Live Demo

You can [try game itself](https://maio.cz/tetris/index.html) or [play with devcards](https://maio.cz/tetris/cards.html).

## Setup

To get an interactive development environment run [Leiningen](http://leiningen.org/):

    lein figwheel dev cards

and open resources/public/index.html or resources/public/cards.html in your browser.

To create a production build run:

    lein do clean, cljsbuild once min min-cards

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

Copyright © 2016 Marian Schubert

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
