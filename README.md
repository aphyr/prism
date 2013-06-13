# Prism

When you save .clj files, Prism automatically reloads namespaces and re-runs
the appropriate clojure.test tests. Like the NSA, it may or may not be snooping on everything you write, and checking to make sure it's not dangerous.

## Usage

Add Prism to `~/.lein/profiles.clj`:

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

## License

Released under the Eclipse Public License; the same as Clojure.
