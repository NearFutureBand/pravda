package pravda.cli.languages

import com.google.protobuf.ByteString
import pravda.vm.ExecutionResult

import scala.language.higherKinds

trait VmLanguage[F[_]] {

  def run(program: ByteString,
          executor: ByteString,
          storagePath: String,
          wattLimit: Long): F[Either[String, ExecutionResult]]
}
