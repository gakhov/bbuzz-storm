(defproject bbuzz-storm "1.0.0-SNAPSHOT"
  :description "bbuzzinga hackathon project"
  :source-path "src/clj"
  :java-source-path "src/jvm"
  :dependencies [
    [com.amazonaws/aws-java-sdk  "1.4.5"]
    [xuggle/xuggle-xuggler "5.4"]
  ]
  :dev-dependencies [
    [org.clojure/clojure "1.4.0"]
    [storm "0.8.2"]]
  :jvm-opts [
      "-Djava.library.path=/usr/local/lib:/opt/local/lib:/usr/lib"
      "-Xmx10G"
  ]
  :min-lein-version "1.6.2"
  :max-lein-version "1.7.4"
  :repositories {"local" ~(str (.toURI (java.io.File. "maven_repository")))}
  :aot :all
)
