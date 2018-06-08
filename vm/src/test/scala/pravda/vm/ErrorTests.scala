package pravda.vm

import utest.TestSuite
import utest._
import VmUtils._
import Opcodes._
import pravda.vm.state.VmError._
import pravda.vm.state.{Environment, VmError}
import pravda.common.domain.Address

object ErrorTests extends TestSuite {

  val tests = Tests {

    def assertError(program: ProgramStub, error: VmError, env: Environment = emptyState): Unit = {
      val e = exec(program, env).error
      assert(e.nonEmpty)
      assert(e.get.error == error)
    }

    'stackUnderflow - {

      'simple - {

        val program = prog.opcode(POP)
        assertError(program, StackUnderflow)

        val program2 = prog.opcode(PUSHX).put(1).opcode(POP).opcode(POP)
        assertError(program2, StackUnderflow)

      }

      'complex - {

        val address = Address.fromHex("0d0f424e")
        val storedProg = prog.opcode(PUSHX).put(111)

        val wState = environment(address -> storedProg)

        val program1 = prog.opcode(PCALL).put(address).put(1)
        assertError(program1, StackUnderflow, env = wState)

        val program2 = prog.opcode(PUSHX).put(4).opcode(PCALL).put(address).put(2)
        assertError(program2, StackUnderflow, env = wState)

      }

    }

    'noSuchProgram - {

      val address = Address.fromByteArray(Array(13.toByte, 15.toByte, 66.toByte, 78.toByte))
      val wrongAddress = Address.fromByteArray(Array(13.toByte, 15.toByte, 0.toByte, 78.toByte))

      val storedProg = prog.opcode(PUSHX).put(111)

      val wState = environment(address -> storedProg)

      val program1 = prog.opcode(PUSHX).put(wrongAddress).opcode(PUSHX).put(0).opcode(PCALL)
      assertError(program1, NoSuchProgram, env = wState)

    }

    'noSuchLibrary - {

      'notExists - {
        val address = Address.fromHex("0d0f424e")
        val wrongAddress  = Address.fromHex("0d0f004e")

        val storedProg = prog.opcode(PUSHX).put(111)

        val wState = environment(address -> storedProg)

        val program1 = prog.opcode(LCALL).put(wrongAddress).put("mypush").put(0)
        assertError(program1, NoSuchLibrary, env = wState)

      }

      'notLibrary - {

        val address = Address.fromHex("0d0f424e")

        val regularProgram = prog.opcode(PUSHX).put(111)

        val wState = environment(address -> regularProgram)

        val program1 = prog.opcode(LCALL).put(address).put("mypush").put(0)
        assertError(program1, NoSuchLibrary, env = wState)

      }

    }

    'externalError - {
        val address = Address.fromHex("0d0f424e")

        val storedProg = prog.opcode(POP)

        val wState = environment(address -> storedProg)

        val program1 = prog.opcode(PCALL).put(address).put(0)
        assertError(program1, StackUnderflow, env = wState)

      }


      'noSuchMethod - {
        val address = Address.fromHex("0d0f424e")

        val libraryMethod = prog.opcode(FTBL).put(1).put("meth1").put(10)

        val wState = environment(address -> libraryMethod)

        val program1 = prog.opcode(LCALL).put(address).put("meth2").put(0)
        assertError(program1, NoSuchMethod, env = wState)

      }

      'operationDenied - {

        val program1 = prog.opcode(SGET).put(0)
        assertError(program1, OperationDenied)

      }
  }
}