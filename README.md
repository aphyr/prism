# Prism

Running tests with `lein test` means waiting for the JVM startup, and waiting
for the project to compile. Prism recompiles and reloads files as you change
them, and re-runs their corresponding tests immediately. Inspired by Midje's
excellent autotest.

## Usage

Check out [Clojars](https://clojars.org/com.aphyr/prism) for the latest
version number, then add Prism to `~/.lein/profiles.clj`:

```
{:user
 {:plugins [[com.aphyr/prism "0.1.0"]]
  :dependencies [[com.aphyr/prism "0.1.0-SNAPSHOT"]]}}
```

You could alternatively add Prism to the dev dependencies in a given
project--but this lets you use Prism against any project regardless of its
project.clj.

Then, just run `lein prism` in your project directory. It'll do a full test
first, then as you write .clj files, it'll re-run the corresponding test
namespace. Prism assumes your tests are named `foo.core-test`.

## Why "Prism"?

It may be snooping on everything you write, and checking to make sure it's not
dangerous. Or maybe not! Exciting!

## License

Released under the Eclipse Public License; the same as Clojure.
