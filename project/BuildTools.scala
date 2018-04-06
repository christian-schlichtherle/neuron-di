import sbt.Keys._
import sbt._
import sbt.IO

/** @author Christian Schlichtherle */
object BuildTools {

  def generateVersionFile: Def.Initialize[Task[Seq[File]]] = {
    Def.task {
      val file = (resourceManaged in Compile).value / "global" / "namespace" / "neuron" / "di" / "sbt" / "plugin" / "version"
      IO.write(file, version.value)
      Seq(file)
    }
  }
}
