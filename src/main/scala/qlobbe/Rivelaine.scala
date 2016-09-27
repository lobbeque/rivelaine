package qlobbe

/*
 * Scala
 */
import scala.util.matching.Regex
import scala.collection.JavaConverters._

/*
 * Jsoup
 */
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object Rivelaine {

  // run --mode "link" --path "http://www.revue-ballast.fr/frederic-lordon-organiser-la-contagion/"
  // run --mode "scrap" --path "http://www.yabiladi.com/articles/details/45605/maroc-espaces-culture-desherence.html"

  val usage = """import scala.collection.JavaConverters._
    Usage :
                --mode (required) [link|scrap]
                --path (required) url or path to html file
              """

  val pattern_no_link = List("""javascript:""".r, """mailto""".r, """#""".r, """addtoany""".r)
  val pattern_social  = List("""facebook.com""".r, """plus.google""".r, """twitter.com""".r)
  val pattern_http    ="""(http|ftp|https)""".r

  def appendMapList(map: Map[String, List[String]], k: String, v: String) : Map[String, List[String]] = {
    map + (k -> ( map(k) ::: List(v)))
  }

  def doesItMatch(word: String, pattern: Regex) : Boolean = {
    val l = pattern findFirstIn word
    ! l.isEmpty
  }

  def doesItMatchOr(word: String, patterns: List[Regex], res: Boolean) : Boolean = {
    if ( patterns.isEmpty )
      res
    else {
      if (!doesItMatch(word, patterns.head))
        doesItMatchOr(word, patterns.tail, res || false)
      else 
        doesItMatchOr(word, patterns.tail, true)
    }
  }

  def splitUrl(url: String) = {
    val splitedUrl = url.replace((pattern_http findFirstIn url).head + "://","")
                        .replace("www.","") 
                        .split("/")
    List(splitedUrl(0),if (!splitedUrl.tail.isEmpty) splitedUrl.tail.mkString("/") else "")
  }

  def getDomainName(url: String) : String = {
    splitUrl(url)(0)
  }

  def getDom(content: String, mode: String) : Document = {
    mode match {
      case "url" =>
        Jsoup.connect(content).get() 
      case "file" =>
        Jsoup.parse(content)
    }
  }

  def getAttr(elementList: List[Object], attributeList: List[String], attributeKey: String) : List[String] = {
    elementList match {
      case Nil => attributeList
      case v :: tail => getAttr(tail, attributeList ::: List(v.asInstanceOf[Element].attr(attributeKey)), attributeKey)
    }
  }

  def getLink(content: String, domainName: String, mode: String) : Map[String, List[String]] = {

    val dom = getDom(content,mode)

    val links = Map("in_path" -> List(), "in_url" -> List(), "out_social" -> List(), "out_url" -> List())

    def grabLink(list: List[String], links: Map[String, List[String]]) : Map[String, List[String]] = {
      list match {
        case Nil => links
        case v :: tail =>
          if (doesItMatchOr(v, pattern_no_link, false))
            grabLink(tail, links)            
          else if (!doesItMatch(v, pattern_http))
            grabLink(tail, appendMapList(links,"in_path",v))
          else if (doesItMatch(v, domainName.r))
            grabLink(tail, appendMapList(links,"in_url",v))
          else if (doesItMatchOr(v, pattern_social, false))
            grabLink(tail, appendMapList(links,"out_social",v))
          else 
            grabLink(tail, appendMapList(links,"out_url",v))
      }
    }

    grabLink(getAttr(dom.select("a").toArray.toList, List[String](), "href"), links)
  }

  def getLinkJava(content: String, domainName: String, mode: String) = {
    val links = getLink(content, domainName, mode)
    Map("in_path" -> links("in_path").asJava, "in_url" -> links("in_url").asJava, "out_social" -> links("out_social").asJava, "out_url" -> links("out_url").asJava).asJava
  }

  def getScraper(path: String, domainName: List[String]) = {

    // val dom = getDom(path,"url")

    // val rules = Rules.rules(domainName(0))

    // def applyRules(rules: Map[String, String], dom: Document) : Map[String, String] = rules map {
    //   case (k, v) => (k,getText(dom,v))
    // }

    // println(applyRules(rules, dom))

    // scrapper le text
    // scrapper différents types de pages connues ( trouver l'échelle site ou cms ou ... )
    // scrapper wild si pas de rêgles ( readability.js )
     
    
  }

  def main(args: Array[String]) {

    if (args.length == 0) println(usage)
    val arglist = args.toList
    type OptionMap = Map[String, String]

    def nextOption(map: OptionMap, list: List[String]) : OptionMap = {
      list match {
        case Nil => map
        case "--mode" :: s :: tail if (s == "link") || (s == "scrap")  =>
          nextOption(map ++ Map("mode" -> s), tail)
        case "--path" :: s :: tail  =>
          nextOption(map ++ Map("path" -> s), tail)
        case option :: tail => println("Unknown option "+option); println(usage); sys.exit(1)
      }
    }

    val options = nextOption(Map(),arglist)

    options("mode") match {
      case "link" =>
        println(getLink(options("path"),getDomainName(options("path")),"url"))
      case "scrap" =>
        getScraper(options("path"),splitUrl(options("path")))
    }
  }
}
