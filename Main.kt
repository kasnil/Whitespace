import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.max

enum class Token {
    Space,
    Tab,
    NewLine,
}

fun Char.toToken() =
    when (this) {
        ' ' -> Token.Space
        '\t' -> Token.Tab
        '\n' -> Token.NewLine
        else -> null
    }

fun Token.toChar() =
    when (this) {
        Token.Space -> ' '
        Token.Tab -> '\t'
        Token.NewLine -> '\n'
    }

enum class Command {
    Push,
    Dup,
    Ref,
    Slide,
    Swap,
    Discard,
    Infix,
    Store,
    Retrieve,
    Label,
    Call,
    Jump,
    If,
    Return,
    OutputChar,
    OutputNum,
    ReadChar,
    ReadNum,
    End,
}

enum class Operation {
    Plus,
    Minus,
    Times,
    Divide,
    Modulo,
}

enum class Test {
    Zero,
    Negative,
}

typealias Label = String

interface Instruction {
    val type: Command
}

class PushInstruction(
    val value: Int,
) : Instruction {
    override val type: Command
        get() = Command.Push
}

class DupInstruction : Instruction {
    override val type: Command
        get() = Command.Dup
}

class RefInstruction(
    val value: Int,
) : Instruction {
    override val type: Command
        get() = Command.Ref
}

class SlideInstruction(
    val value: Int,
) : Instruction {
    override val type: Command
        get() = Command.Slide
}

class SwapInstruction : Instruction {
    override val type: Command
        get() = Command.Swap
}

class DiscardInstruction : Instruction {
    override val type: Command
        get() = Command.Discard
}

class InfixInstruction(
    val operation: Operation,
) : Instruction {
    override val type: Command
        get() = Command.Infix
}

class StoreInstruction : Instruction {
    override val type: Command
        get() = Command.Store
}

class RetrieveInstruction : Instruction {
    override val type: Command
        get() = Command.Retrieve
}

class LabelInstruction(
    val label: Label,
) : Instruction {
    override val type: Command
        get() = Command.Label
}

class CallInstruction(
    val label: Label,
) : Instruction {
    override val type: Command
        get() = Command.Call
}

class JumpInstruction(
    val label: Label,
) : Instruction {
    override val type: Command
        get() = Command.Jump
}

class IfInstruction(
    val test: Test,
    val label: Label,
) : Instruction {
    override val type: Command
        get() = Command.If
}

class ReturnInstruction : Instruction {
    override val type: Command
        get() = Command.Return
}

class OutputCharInstruction : Instruction {
    override val type: Command
        get() = Command.OutputChar
}

class OutputNumInstruction : Instruction {
    override val type: Command
        get() = Command.OutputNum
}

class ReadCharInstruction : Instruction {
    override val type: Command
        get() = Command.ReadChar
}

class ReadNumInstruction : Instruction {
    override val type: Command
        get() = Command.ReadNum
}

class EndInstruction : Instruction {
    override val type: Command
        get() = Command.End
}

typealias Program = List<Instruction>
typealias Heap = MutableList<Int>
typealias Stack = java.util.Stack<Int>

class Compiler(
    val code: String,
) {
    fun compile(): List<Instruction> {
        val tokens = tokenise(code)
        val instructions = parse(tokens)

        return instructions
    }

    private fun tokenise(code: String): List<Token> {
        var position = 0
        val tokens: MutableList<Token> = mutableListOf()

        while (position < code.length) {
            val character = code[position]

            val token = character.toToken()
            when (token) {
                Token.Space, Token.Tab, Token.NewLine -> tokens.add(token)
                else -> {}
            }
            position++
        }

        return tokens
    }

    private fun parse(tokens: List<Token>): List<Instruction> {
        val instructions: MutableList<Instruction> = mutableListOf()

        if (tokens.isEmpty()) {
            return instructions
        }

        var position = 0
        while (position < tokens.size) {
            if ((position + 1) < tokens.size &&
                tokens[position] == Token.Space &&
                tokens[position + 1] == Token.Space
            ) {
                val pair = parseNumber(tokens, position + 2)
                instructions += PushInstruction(pair.first)
                position = pair.second + 1
            } else if ((position + 2) < tokens.size &&
                tokens[position] == Token.Space &&
                tokens[position + 1] == Token.NewLine &&
                tokens[position + 2] == Token.Space
            ) {
                instructions += DupInstruction()
                position += 3
            } else if ((position + 2) < tokens.size &&
                tokens[position] == Token.Space &&
                tokens[position + 1] == Token.Tab &&
                tokens[position + 2] == Token.Space
            ) {
                val pair = parseNumber(tokens, position + 3)
                instructions += RefInstruction(pair.first)
                position = pair.second + 1
            } else if ((position + 2) < tokens.size &&
                tokens[position] == Token.Space &&
                tokens[position + 1] == Token.Tab &&
                tokens[position + 2] == Token.NewLine
            ) {
                val pair = parseNumber(tokens, position + 3)
                instructions += SlideInstruction(pair.first)
                position = pair.second + 1
            } else if ((position + 2) < tokens.size &&
                tokens[position] == Token.Space &&
                tokens[position + 1] == Token.NewLine &&
                tokens[position + 2] == Token.Tab
            ) {
                instructions += SwapInstruction()
                position += 3
            } else if ((position + 2) < tokens.size &&
                tokens[position] == Token.Space &&
                tokens[position + 1] == Token.NewLine &&
                tokens[position + 2] == Token.NewLine
            ) {
                instructions += DiscardInstruction()
                position += 3
            } else if ((position + 3) < tokens.size &&
                tokens[position] == Token.Tab &&
                tokens[position + 1] == Token.Space &&
                tokens[position + 2] == Token.Space &&
                tokens[position + 3] == Token.Space
            ) {
                instructions += InfixInstruction(Operation.Plus)
                position += 4
            } else if ((position + 3) < tokens.size &&
                tokens[position] == Token.Tab &&
                tokens[position + 1] == Token.Space &&
                tokens[position + 2] == Token.Space &&
                tokens[position + 3] == Token.Tab
            ) {
                instructions += InfixInstruction(Operation.Minus)
                position += 4
            } else if ((position + 3) < tokens.size &&
                tokens[position] == Token.Tab &&
                tokens[position + 1] == Token.Space &&
                tokens[position + 2] == Token.Space &&
                tokens[position + 3] == Token.NewLine
            ) {
                instructions += InfixInstruction(Operation.Times)
                position += 4
            } else if ((position + 3) < tokens.size &&
                tokens[position] == Token.Tab &&
                tokens[position + 1] == Token.Space &&
                tokens[position + 2] == Token.Tab &&
                tokens[position + 3] == Token.Space
            ) {
                instructions += InfixInstruction(Operation.Divide)
                position += 4
            } else if ((position + 3) < tokens.size &&
                tokens[position] == Token.Tab &&
                tokens[position + 1] == Token.Space &&
                tokens[position + 2] == Token.Tab &&
                tokens[position + 3] == Token.Tab
            ) {
                instructions += InfixInstruction(Operation.Modulo)
                position += 4
            } else if ((position + 2) < tokens.size &&
                tokens[position] == Token.Tab &&
                tokens[position + 1] == Token.Tab &&
                tokens[position + 2] == Token.Space
            ) {
                instructions += StoreInstruction()
                position += 3
            } else if ((position + 2) < tokens.size &&
                tokens[position] == Token.Tab &&
                tokens[position + 1] == Token.Tab &&
                tokens[position + 2] == Token.Tab
            ) {
                instructions += RetrieveInstruction()
                position += 3
            } else if ((position + 2) < tokens.size &&
                tokens[position] == Token.NewLine &&
                tokens[position + 1] == Token.Space &&
                tokens[position + 2] == Token.Space
            ) {
                val pair = parseString(tokens, position + 3)
                instructions += LabelInstruction(pair.first)
                position = pair.second + 1
            } else if ((position + 2) < tokens.size &&
                tokens[position] == Token.NewLine &&
                tokens[position + 1] == Token.Space &&
                tokens[position + 2] == Token.Tab
            ) {
                val pair = parseString(tokens, position + 3)
                instructions += CallInstruction(pair.first)
                position = pair.second + 1
            } else if ((position + 2) < tokens.size &&
                tokens[position] == Token.NewLine &&
                tokens[position + 1] == Token.Space &&
                tokens[position + 2] == Token.NewLine
            ) {
                val pair = parseString(tokens, position + 3)
                instructions += JumpInstruction(pair.first)
                position = pair.second + 1
            } else if ((position + 2) < tokens.size &&
                tokens[position] == Token.NewLine &&
                tokens[position + 1] == Token.Tab &&
                tokens[position + 2] == Token.Space
            ) {
                val pair = parseString(tokens, position + 3)
                instructions += IfInstruction(Test.Zero, pair.first)
                position = pair.second + 1
            } else if ((position + 2) < tokens.size &&
                tokens[position] == Token.NewLine &&
                tokens[position + 1] == Token.Tab &&
                tokens[position + 2] == Token.Tab
            ) {
                val pair = parseString(tokens, position + 3)
                instructions += IfInstruction(Test.Negative, pair.first)
                position = pair.second + 1
            } else if ((position + 2) < tokens.size &&
                tokens[position] == Token.NewLine &&
                tokens[position + 1] == Token.Tab &&
                tokens[position + 2] == Token.NewLine
            ) {
                instructions += ReturnInstruction()
                position += 3
            } else if ((position + 2) < tokens.size &&
                tokens[position] == Token.NewLine &&
                tokens[position + 1] == Token.NewLine &&
                tokens[position + 2] == Token.NewLine
            ) {
                instructions += EndInstruction()
                position += 3
            } else if ((position + 3) < tokens.size &&
                tokens[position] == Token.Tab &&
                tokens[position + 1] == Token.NewLine &&
                tokens[position + 2] == Token.Space &&
                tokens[position + 3] == Token.Space
            ) {
                instructions += OutputCharInstruction()
                position += 4
            } else if ((position + 3) < tokens.size &&
                tokens[position] == Token.Tab &&
                tokens[position + 1] == Token.NewLine &&
                tokens[position + 2] == Token.Space &&
                tokens[position + 3] == Token.Tab
            ) {
                instructions += OutputNumInstruction()
                position += 4
            } else if ((position + 3) < tokens.size &&
                tokens[position] == Token.Tab &&
                tokens[position + 1] == Token.NewLine &&
                tokens[position + 2] == Token.Tab &&
                tokens[position + 3] == Token.Space
            ) {
                instructions += ReadCharInstruction()
                position += 4
            } else if ((position + 3) < tokens.size &&
                tokens[position] == Token.Tab &&
                tokens[position + 1] == Token.NewLine &&
                tokens[position + 2] == Token.Tab &&
                tokens[position + 3] == Token.Tab
            ) {
                instructions += ReadNumInstruction()
                position += 4
            } else {
                error("Unrecognised input")
            }
        }
        return instructions
    }

    private fun parseString(
        tokens: List<Token>,
        fromPosition: Int,
    ): Pair<String, Int> {
        val stringTokens = mutableListOf<Token>()
        for (currentPosition in fromPosition..<tokens.size) {
            if (tokens[currentPosition] == Token.NewLine) {
                return Pair(makeString(stringTokens), currentPosition)
            }
            stringTokens += tokens[currentPosition]
        }

        error("Undefined string")
    }

    private fun makeString(tokens: List<Token>) = tokens.joinToString("") { it.toChar().toString() }.reversed()

    private fun parseNumber(
        tokens: List<Token>,
        fromPosition: Int,
    ): Pair<Int, Int> {
        val numberTokens = mutableListOf<Token>()
        for (currentPosition in fromPosition..<tokens.size) {
            if (tokens[currentPosition] == Token.NewLine) {
                return Pair(makeNumber(numberTokens), currentPosition)
            }
            numberTokens += tokens[currentPosition]
        }

        error("Undefined number")
    }

    private fun makeNumber(tokens: List<Token>): Int {
        val sign = if (tokens.first() == Token.Space) 1 else -1
        val tokensWithoutFirst = tokens.drop(1)
        return sign *
            tokensWithoutFirst.foldIndexed(0) { index, accumulator, bit ->
                if (bit == Token.Tab) {
                    accumulator + (1 shl (tokensWithoutFirst.size - 1 - index))
                } else {
                    accumulator
                }
            }
    }
}

class VM(
    val instructions: Program,
    val input: InputStream,
    val output: OutputStream,
) {
    private val valStack: Stack
    private val callStack: Stack
    private var memory: Heap
    private var ip: Int

    init {
        valStack = Stack()
        callStack = Stack()
        memory = mutableListOf()
        ip = 0
    }

    fun execute() {
        while (ip < instructions.size) {
            val instruction = instructions[ip]
            if (instruction is PushInstruction) {
                valStack.push(instruction.value)
            } else if (instruction is DupInstruction) {
                val n = valStack.peek()
                valStack.push(n)
            } else if (instruction is RefInstruction) {
                val n = valStack.elementAt(instruction.value)
                valStack.push(n)
            } else if (instruction is SlideInstruction) {
                val n = valStack.pop()
                valStack.remove(instruction.value)
                valStack.push(n)
            } else if (instruction is SwapInstruction) {
                val n = valStack.pop()
                val m = valStack.pop()
                valStack.push(n)
                valStack.push(m)
            } else if (instruction is DiscardInstruction) {
                valStack.pop()
            } else if (instruction is InfixInstruction) {
                val y = valStack.pop()
                val x = valStack.pop()
                val n =
                    when (instruction.operation) {
                        Operation.Plus -> x + y
                        Operation.Minus -> x - y
                        Operation.Times -> x * y
                        Operation.Divide -> x / y
                        Operation.Modulo -> x % y
                    }
                valStack.push(n)
            } else if (instruction is OutputCharInstruction) {
                val n = valStack.pop()
                outputChar(n)
            } else if (instruction is ReadCharInstruction) {
                val n = valStack.pop()
                readChar(n)
            } else if (instruction is ReadNumInstruction) {
                val n = valStack.pop()
                readNum(n)
            } else if (instruction is OutputNumInstruction) {
                val n = valStack.pop()
                outputNum(n)
            } else if (instruction is LabelInstruction) {
            } else if (instruction is CallInstruction) {
                val l = instruction.label
                val loc = findLabel(l)
                callStack.push(ip)
                ip = loc
                continue
            } else if (instruction is JumpInstruction) {
                val l = instruction.label
                val loc = findLabel(l)
                ip = loc
                continue
            } else if (instruction is IfInstruction) {
                val n = valStack.pop()
                val l = instruction.label
                val ok =
                    when (instruction.test) {
                        Test.Zero -> n == 0
                        Test.Negative -> n < 0
                    }
                if (ok) {
                    val loc = findLabel(l)
                    ip = loc
                    continue
                }
            } else if (instruction is ReturnInstruction) {
                val c = callStack.pop()
                ip = c
            } else if (instruction is StoreInstruction) {
                val n = valStack.pop()
                val loc = valStack.pop()
                store(n, loc)
            } else if (instruction is RetrieveInstruction) {
                val loc = valStack.pop()
                val value = retrieve(loc)
                valStack.push(value)
            } else if (instruction is EndInstruction) {
                break
            } else {
                error("Unrecognised input")
            }
            ip++
        }
    }

    private fun readNum(loc: Int) {
        var valueStr = ""
        while (true) {
            val ascii = input.read()
            if (ascii == 10) {
                if (valueStr.isEmpty()) {
                    error("Wrong num bytes read")
                }
                break
            }
            valueStr += ascii.toChar()
        }
        val value = valueStr.toIntOrNull() ?: error("Wrong num bytes read")
        store(value, loc)
    }

    private fun outputNum(num: Int) {
        num.toString().forEach { outputChar(it.code) }
    }

    private fun readChar(loc: Int) {
        val value = input.read()

        check(value != -1) { "Wrong num bytes read" }

        store(value, loc)
    }

    private fun outputChar(byte: Int) {
        output.write(byte)
        output.flush()
    }

    private fun findLabel(label: Label): Int {
        for (i in instructions.indices) {
            val instruction = instructions[i]
            if (instruction is LabelInstruction) {
                if (instruction.label == label) {
                    return i
                }
            }
        }
        error("Undefined label ($label)")
    }

    private fun store(
        newValue: Int,
        pos: Int,
    ) {
        val newSize = max(memory.size, pos + 1)
        if (newSize > memory.size) {
            val newHeap = MutableList(newSize) { 0 }

            memory.forEachIndexed { index, value -> newHeap[index] = value }

            memory = newHeap
        }
        memory[pos] = newValue
    }

    private fun retrieve(pos: Int) = memory[pos]
}

fun main(vararg args: String) {
    if (args.size != 1) {
        println("No fileName given")
        return
    }
    val fileName = args[0]
    val inputStream = FileInputStream(fileName)
    val code = String(inputStream.readAllBytes())

    val compiler = Compiler(code)
    val instructions = compiler.compile()

    val machine = VM(instructions, System.`in`, System.out)
    machine.execute()
}
