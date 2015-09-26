package com.github.div082.jsdoc


import scala.io.Source
import java.io.PrintWriter
import scopt._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import scala.util.parsing.combinator.{RegexParsers, PackratParsers}
import scala.util.parsing.input.CharSequenceReader


case class Record(tag: String, rType: String, name: String, desc: String)


/**
 * JsDoc Parser
 */
class JSDocParser extends RegexParsers with PackratParsers {
  type RTag = String
  type RType = String
  type RName = String
  type RDesc = String

  lazy val word = """(\w|\.|\,|\:|\;|\!|\*|\(|\)|\<|\>)+""".r

  lazy val tag: PackratParser[RTag] = "@" ~> word ^^ (w => w)

  lazy val name: PackratParser[RName] = word ^^ (w => w)

  lazy val nestedParentheses: PackratParser[String] =
    "{" ~ rep1(word | nestedParentheses) ~ "}" ^^ {
      case "{" ~ w ~ "}"  => "{" + w.mkString(" ") + "}"
    }

  lazy val rType: PackratParser[RType] =
    "{" ~> rep1(word | nestedParentheses) <~ "}" ^^ (w => w.mkString(""))

  lazy val description: PackratParser[RDesc] = """[^@]+""".r ^^ (w => w.toString.trim)

  lazy val record: PackratParser[Record] = {
    tag ~ opt(rType ~ opt(name)) ~ opt(description) ^^ {
      case t ~ None        ~ d => Record(t, "", "", d.getOrElse(""))
      case t ~ Some(r ~ n) ~ d => Record(t, r, n.getOrElse(""), d.getOrElse(""))
    }
  }

  lazy val desc: PackratParser[Record] = description ^^ (desc => Record("", "", "", desc))

  lazy val block: PackratParser[List[Record]] = rep(record | desc)

  def parseAll[T](p: Parser[T], input: String): ParseResult[T] = {
    phrase(p)(new PackratReader[Char](new CharSequenceReader(input)))
  }

}


/**
 * JSDoc Parser object
 */
object JSDocParser {
  val COMMENT_PATTERN = """/\*(?:(?!\*\/).|[\n\r])*\*\/""".r

  case class Config(in: List[String] = List(), out: String = "result.json")

  /**
   * Mapper for reading/saving json
   */
  object JSONMapper extends ObjectMapper with ScalaObjectMapper {
    this.registerModule(DefaultScalaModule)
  }

  val cmdParser = new scopt.OptionParser[Config]("jsdoc-parser") {
    opt[String]('o', "out")
      .optional()
      .valueName("<output>")
      .action {
        (x, c) => c.copy(out = x)
      }
      .text("path to output file")

    arg[String]("<path>...")
      .required()
      .unbounded()
      .action {
        (x, c) => c.copy(in = c.in :+ x)
      }
      .text("paths to input files")

    help("help")
      .text("prints this usage text")
  }

  /**
   * @param path Path to js file
   * @return List of JSDocs
   */
  private def readJSDocs(path: String): List[String] = {
    val source = Source.fromFile(path, "UTF-8")
    val text = try source.mkString finally source.close()
    val jsDocs = COMMENT_PATTERN.findAllIn(text)


    (for (jsDoc <- jsDocs)
      yield jsDoc.replaceAll("^\\/\\*\\*", "")
        .replaceAll("\\*\\/$", "")
        .replaceAll("\\n\\s*\\*", "")
        .trim).toList
  }

  /**
   * @param records Records to save
   * @param path Path to result file
   */
  private def saveJson(records: List[List[Record]], path: String) = {
    val out = new PrintWriter(path)
    out.write(JSONMapper.writeValueAsString(records))
    out.close()
  }

  /**
   * @param args Program arguments
   */
  def main(args: Array[String]): Unit = {
    cmdParser.parse(args, Config()) match {
      case Some(config: Config) =>
        val jsdocParser = new JSDocParser()
        val result = for (path <- config.in)
          yield for (jsDoc <- readJSDocs(path))
            yield jsdocParser.parseAll(jsdocParser.block, jsDoc).get

        saveJson(result.flatten, config.out)

      case None => new Exception(s"Invalid arguments")
    }
  }
}
