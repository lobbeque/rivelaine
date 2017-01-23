package qlobbe

/*
 * Scala
 */
import scala.util.matching.Regex
import scala.collection.JavaConverters._
import scala.util.control.Breaks._

/*
 * Java
 */
import java.text.SimpleDateFormat
import java.util.Date

/*
 * Jsoup
 */
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Tag

object Rivelaine {

  // run --mode "link" --path "http://www.revue-ballast.fr/frederic-lordon-organiser-la-contagion/"
  // run --mode "scrap" --path "http://www.yabiladi.com/articles/details/45605/maroc-espaces-culture-desherence.html"

  val usage = """import scala.collection.JavaConverters._
    Usage :
                --mode (required) [link|content]
                --path (required) url or path to html file
              """

  val pattern_no_link           = List("""javascript:""".r, """mailto""".r, """#""".r, """addtoany""".r)
  val pattern_social            = List("""facebook.com""".r, """plus.google""".r, """twitter.com""".r)
  val pattern_http              = """(http|ftp|https)""".r
  val pattern_metaTitle         = List("title","meta[name=twitter:title]","meta[property=og:title]")
  val pattern_metaDesc          = List("meta[name=description]","meta[name=twitter:description]","meta[property=og:description]")
  val pattern_metaImg           = List("meta[name=twitter:image]","meta[property=og:image]")
  val pattern_metaTwitCreator   = List("meta[name=twitter:creator]")
  val pattern_metaPublishedTime = List("meta[property=article:published_time]")
  val pattern_metaPublisher     = List("meta[property=article:publisher]") 
  val pattern_writtenBy         = """(byline|author|writtenby|p-author|pseudo|avatar|auteur)""".r 
  val pattern_social_span       = """(social|twitter)""".r

  val pattern_content_meta      = """(byline|author|writtenby|p-author|pseudo|avatar|title|titre|date|auteur|descriptif)""".r
  val pattern_content_comm      = """(comment|commentaire)""".r

  /*
   * Body Patterns
   */
  val pattern_title    = """(title|titre|h1)""".r 
  val pattern_author   = """(byline|author|writtenby|pseudo|avatar|auteur)""".r 
  val pattern_date     = """(date|time$)""".r 
  val pattern_meta     = """(descriptif|description)""".r
  val pattern_comm     = """(comment|commentaire)""".r
  val pattern_keep     = """(content|and|article|body|column|main|shadow|discussion|post|forum|comment|bloc)""".r
  val pattern_avoid    = """(bt$|recommended|most|community|nav|category|bar|popular|button|posting|playlist|disqus|extra|similar|preview|header|legends|related|remark|agegate|toolbar|outil|banner|update|combx|footer|foot|menu|modal|rss|shoutbox|sidebar|skyscraper|sponsor|ad-break|pagination|pager|popup)""".r
  val pattern_remove   = """(sidebar|navbare|menu|playlist|nav)""".r
  val pattern_text     = List("section","h2","h3","h4","h5","h6","p","td","pre","b")
  val pattern_para     = List("section","h2","h3","h4","h5","h6","p","td","tr","pre","b","br","ul","li","a","img")  

  /*
   * Clean patterns 
   */
  val pattern_clean_author = """(,|\[ MP \]|par |by )""".r
  val pattern_clean_date   = """(?:(?:31(\/|-|\.)(?:0?[13578]|1[02]|(?:Jan|Mar|May|Mai|Jul|Juillet|Aug|Aou|Oct|Dec)))\1|(?:(?:29|30)(\/|-|\.)(?:0?[1,3-9]|1[0-2]|(?:Jan|Mar|Apr|Avr|May|Mai|Jun|Juin|Jul|Aug|Aou|Sep|Oct|Nov|Dec))\2))(?:(?:1[6-9]|[2-9]\d)?\d{2})$|^(?:29(\/|-|\.)(?:0?2|(?:Feb))\3(?:(?:(?:1[6-9]|[2-9]\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\d|2[0-8])(\/|-|\.)(?:(?:0?[1-9]|(?:Jan|Feb|Fev|Mar|Apr|Avr|May|Mai|Jun|Juin|Jul|Juillet|Aou|Aug|Sep))|(?:1[0-2]|(?:Oct|Nov|Dec)))\4(?:(?:1[6-9]|[2-9]\d)?\d{2})""".r

  /*
   * Date parsing (regex,dateFormat)
   */

  val date_match = List(("""[0-3][0-9]\/[0-9]{2}\/[0-9]{4}""".r,"dd/MM/yyyy"), // 17/11/1989
                        ("""[0-1][0-9]\/[0-9]{2}\/[0-9]{4}""".r,"MM/dd/yyyy"), // 11/17/1989
                        ("""[0-3][0-9]\.[0-9]{2}\.[0-9]{4}""".r,"dd.MM.yyyy"), // 17.11.1989
                        ("""[0-1][0-9]\.[0-9]{2}\.[0-9]{4}""".r,"MM.dd.yyyy"), // 11.17.1989
                        ("""[0-3][0-9]\/[0-9]{2}\/[0-9]{2}""".r,"dd/MM/yy"), // 17/11/89
                        ("""[0-1][0-9]\/[0-9]{2}\/[0-9]{2}""".r,"MM/dd/yy"), // 11/17/89
                        ("""[0-3][0-9]\.[0-9]{2}\.[0-9]{2}""".r,"dd.MM.yy"), // 17.11.89
                        ("""[0-1][0-9]\.[0-9]{2}\.[0-9]{2}""".r,"MM.dd.yy"), // 11.17.89
                        ("""[0-1][0-9]( *)(jan|fev|mar|may|avr|apr|mai|may|jun|jul|jui|aug|aou|aoû|sep|oct|nov|dec)[a-z ]+[0-9]{4}""".r,"dd MMMM yyyy"), // 17 novembre 1989
                        ("""(jan|fev|mar|may|avr|apr|mai|may|jun|jul|jui|aug|aou|aoû|sep|oct|nov|dec)[a-z ]+[0-3][0-9][, ]+[0-9]{4}""","MMMM dd, yyyy")  // novembre 17, 1989
                        )

  def appendMapList(map: Map[String, List[String]], k: String, v: String) : Map[String, List[String]] = {
    map + (k -> ( map(k) ::: List(v)))
  }

  /*
   * Exact match between word and regex 
   */
  def doesItMatch(word: String, pattern: Regex) : Boolean = {
    val l = pattern findFirstIn word
    ! l.isEmpty
  }

  /*
   * Exact match between word and list of regexs ( need a default boolean res ) 
   */
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
    if ( !splitedUrl.isEmpty )
      List(splitedUrl(0),if (!splitedUrl.tail.isEmpty) splitedUrl.tail.mkString("/") else "")
    else 
      List("")
  }

  def getDomainName(url: String) : String = {
    splitUrl(url)(0)
  }

  def getDom(content: String, mode: String) : Document = {
    mode match {
      case "url" =>
        Jsoup.connect(content).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36").get() 
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

  def removeNodes(dom: Document, nodes: List[String]) : Document = {
    nodes match {
      case Nil => dom
      case node :: tail => 
        dom.select(node).remove()
        removeNodes(dom,tail)
    }
  }

  def getNodeVal(dom: Elements, patterns: List[String]) : String = {
    patterns match {
      case Nil => ""
      case pattern :: tail => 
        if (doesItMatch(pattern,"meta".r)) {
          Option(dom.select(pattern).attr("content")) match {
            case None => getNodeVal(dom,tail)
            case Some(t) => t 
          }
        } else {
          Option(dom.select(pattern).text()) match {
            case None => getNodeVal(dom,tail)
            case Some(t) => t 
          }
        }       
    } 
  }

  def getMetadata(dom: Elements) : Map[String,String] = {
    var meta = Map[String,String]()
    meta += "title" -> getNodeVal(dom,pattern_metaTitle)
    meta += "description" -> getNodeVal(dom,pattern_metaDesc)
    meta += "image" -> getNodeVal(dom,pattern_metaImg)
    meta += "twitter_creator" -> getNodeVal(dom,pattern_metaTwitCreator)
    meta += "published_time" -> getNodeVal(dom,pattern_metaPublishedTime)
    meta += "publisher" -> getNodeVal(dom,pattern_metaPublisher)
    meta
  }

  /*
   * Is it a "written by" node ?
   */
  def isAuthor(node: Element, nodeId: String) : Boolean = {
    (node.attr("rel") == "author" || doesItMatch(nodeId, pattern_author)) && node.text().length > 1
  }

  def isDate(node: Element, nodeId: String) : Boolean = {
    doesItMatch(nodeId, pattern_date) &&
    node.text().length > 1 &&
    !doesItMatch(nodeId,pattern_social_span)
  }

  def isMeta(node: Element, nodeId: String) : Boolean = {
    doesItMatch(nodeId, pattern_meta) &&
    node.text().length > 1 &&
    !doesItMatch(nodeId,pattern_social_span)
  }  

  def isComm(node: Element, nodeId: String) : Boolean = {
    doesItMatch(nodeId, pattern_comm) &&
    node.text().length > 1 &&
    !doesItMatch(nodeId,pattern_social_span)
  }  

  def getSiblingId(node: Element, res: String) : String = {
    if (hasSibling(node)) {
      getSiblingId(node.nextElementSibling(), res + " " + node.nodeName() + " " + node.attr("id") + " " + node.attr("class"))
    } else {
      res
    }
  }  

  /*
   * Avoid some node that are not content candidate 
   */
  def shouldAvoid(node: Element, nodeId: String) : Boolean = {

    val childrenId = if (hasChild(node)) getSiblingId(node.child(0), "") else ""

    doesItMatch(nodeId,pattern_avoid) &&
    !doesItMatchOr(nodeId,List(pattern_keep, pattern_title, pattern_author, pattern_date, pattern_meta, pattern_comm), false) &&
    !doesItMatchOr(childrenId,List(pattern_keep, pattern_title, pattern_author, pattern_date, pattern_meta, pattern_comm), false) &&
    node.tag().toString() != "body" &&
    node.tag().toString() != "a" &&
    node.text().length < 10
  }

  def shouldRemove(node: Element, nodeId: String) : Boolean = {
    doesItMatch(nodeId,pattern_remove)
  }  

  def isTitle(node: Element, nodeId: String) : Boolean = {
    doesItMatch(nodeId,pattern_title) &&
      !doesItMatch(nodeId,pattern_social_span) &&
      node.text().length > 1 &&
      node.tag().toString != "span"
  }

  def isNull(node: Element) : Boolean = {
    Option(node) match {
      case Some(n) => false
      case None => true
    }
  }

  def hasChild(node: Element) : Boolean = {
    Option(node.children().first()) match {
      case Some(c) => true
      case None => false
    }
  }

  def hasSibling(node: Element) : Boolean = {
    if (isNull(node)) {
      false
    } else {
      Option(node.nextElementSibling()) match {
        case Some(c) => true
        case None => false
      }
    }
  }

  def hasParent(node: Element) : Boolean = {
    Option(node.parent()) match {
      case Some(c) => true
      case None => false
    }
  }

  def areTextNodesBlank(textNodes: List[TextNode],areThey: Boolean) : Boolean = {
    textNodes match {  
      case Nil => areThey
      case textNode :: tail =>   
       areTextNodesBlank(tail, areThey && textNode.isBlank())
    }
  }

  def isText(node: Element) : Boolean = {
    (pattern_text.indexOf(node.tag().toString()) != -1 && node.text().length > 1) || (doesItMatch(node.attr("class"),"""com-content""".r))
  }

  def isTextNoTag(node: Element) : Boolean = {
    hasChild(node) && node.childNode(0).nodeName == "#text" && node.childNode(0).toString().length > 1
  }

  def isParagraph(node: Element, nodeId: String) : Boolean = {
    !doesItMatch(nodeId,pattern_avoid) &&
    (node.tag().toString() == "div") &&
    hasChild(node) &&
    (node.text().length > 20) &&
    node.children().toArray().map(child => pattern_para.indexOf(child.asInstanceOf[Element].tag().toString()) != -1).foldLeft(true)(_ && _)
  }

  def getNextNode(node: Element, depthFirst: Boolean) : Element = {

    var nodeCp = node

    if (depthFirst && hasChild(node)) {
      node.children().first()
    } else if (hasSibling(node)) {
      node.nextElementSibling()
    } else {
      do {
        nodeCp = nodeCp.parent()
      } while (!isNull(nodeCp) && !hasSibling(nodeCp))
      if(hasSibling(nodeCp)) {
        nodeCp.nextElementSibling()
      } else {
        nodeCp
      }
      
    }
  }

  def getNbParents(node: Element) : Int = {
    node.parents().size()
  }

  def getNbChildren(node: Element, nb: Int) : Int = {
    if (hasChild(node)) {
      getNbChildren(node.children().first(),nb + 1)
    } else {
      nb
    }
  }

  def getArticleTitle(dom: Document) : String = {
    
    val title = dom.title()
    
    var tmp = title

    if (doesItMatch(tmp,"""[\|\-]""".r)) {
      tmp = """(.*)[\|\-] .*""".r.replaceFirstIn(title,"$1")

      if (tmp.split(" ").length < 3) {
        tmp = """[^\|\-]*[\|\-](.*)""".r.replaceFirstIn(title,"$1")
      }

    } else if (doesItMatch(tmp,""": """.r)) {
      
      val h12s = dom.getElementsByTag("h1").asScala.toList ::: dom.getElementsByTag("h2").asScala.toList
      
      def isInnerTextEqual(h12s: List[Element], title: String, isIt: Boolean) : Boolean = {
        h12s match {  
          case Nil => isIt
          case h12 :: tail =>   
           isInnerTextEqual(tail, title, isIt || (h12.text() == title))
        }        
      }

      if (!isInnerTextEqual(h12s,title,false)) {
        tmp = title.substring(title.lastIndexOf(':') + 1)

        if (tmp.split(" ").length < 3) {
          tmp = title.substring(title.indexOf(':') + 1)
        }
      }
    } else if (tmp.length > 150 || tmp.length < 15) {
      
      val h1s = dom.getElementsByTag("h1").asScala.toList

      if (h1s.size == 1) {
        tmp = h1s.head.text()
      }
    }

    tmp.trim()

    if (tmp.split(" ").length <= 4) {
      tmp = title
    }

    tmp
  }

  /*
   * Remove or Select a node ( and its children ) based on "tag + id + class" names
   */

  def cleanDom(node: Element, selected: List[Tuple3[Element,String,Int]]) : List[Tuple3[Element,String,Int]] = {
    
    if (isNull(node)) {
      
      // No more node, return all selected nodes 
      
      selected
    
    } else {

      // Test the nature of the node
      
      val nodeId = (node.nodeName() + " " + node.attr("id") + " " + node.attr("class")).toLowerCase()

      if (shouldRemove(node,nodeId)) {
        cleanDom(getNextNode(node,false),selected)      

      } else if (shouldAvoid(node,nodeId)) {
        cleanDom(getNextNode(node,false),selected)
      
      } else if (isTitle(node,nodeId)) {
        cleanDom(getNextNode(node,false),selected :+ new Tuple3(node,"title",getNbParents(node)))
      
      } else if (isAuthor(node,nodeId)) {
        cleanDom(getNextNode(node,false),selected :+ new Tuple3(node,"author",getNbParents(node))) 
      
      } else if (isDate(node,nodeId)) {
        cleanDom(getNextNode(node,false),selected :+ new Tuple3(node,"date",getNbParents(node)))

      } else if (isMeta(node, nodeId)) {
        cleanDom(getNextNode(node,false),selected :+ new Tuple3(node,"meta",getNbParents(node)))            

      } else if (isParagraph(node, nodeId)) {
        cleanDom(getNextNode(node,false),selected :+ new Tuple3(node,"text",getNbParents(node)))

      } else if (isText(node)) {
        cleanDom(getNextNode(node,false),selected :+ new Tuple3(node,"text",getNbParents(node))) 

      } else if (isTextNoTag(node)) {
        cleanDom(getNextNode(node,false),
                 selected :+ new Tuple3(new Element(Tag.valueOf("p"), "").text(node.text()),
                 "text",getNbParents(node)))                   
      
      } else {
        cleanDom(getNextNode(node,true),selected)
      }

    }
  }

  def groupByMask(sequences : List[(String,List[(String,String,String)])],
                  mask      : String,
                  content   : List[List[(String,String,String)]],
                  contents  : List[List[List[(String,String,String)]]]
                  ) : List[List[List[(String,String,String)]]] = {
    sequences match {
      case Nil => contents :+ content
      case seq :: tail => 
        if (content.isEmpty || (seq._1 == mask)){
          groupByMask(tail, 
                      seq._1, 
                      content :+ seq._2, 
                      contents)
        } else {
          groupByMask(tail, 
                      seq._1, 
                      List[List[(String,String,String)]]() :+ seq._2, 
                      contents :+ content)
        }
    } 
  }

  def groupBySeq(nodes     : List[(Element,String,Int)], 
                 seq       : List[(Element,String,Int)], 
                 seqMask   : List[String], 
                 sequences : List[(String,List[(String,String,String)])]
                 ) : List[List[List[(String,String,String)]]] = {
    nodes match {  
      case Nil =>
        
        if (seq.isEmpty) {
          groupByMask(sequences, "", List[List[(String,String,String)]](), List[List[List[(String,String,String)]]]())
        } else {
          groupByMask(sequences :+ (seqMask.reduceLeft(_ + _),seq.map(s => (s._1.text(),s._2,s._1.nodeName()))), "", List[List[(String,String,String)]](), List[List[List[(String,String,String)]]]())
        }
        
      case node :: tail =>
        if (seq.isEmpty) {
          groupBySeq(tail, seq :+ node, seqMask :+ node._3.toString() :+ node._2, sequences)
        } else if (
                  (doesItMatch(seq.last._2,"""(title|author|date|meta)""".r) 
                    && doesItMatch(node._2,"""(title|author|date|meta)""".r) 
                    && seq.last._3 == node._3) ||
                  (doesItMatch(seq.last._2,"""(title|author|date|meta)""".r) 
                    && doesItMatch(node._2,"""(text)""".r))
                  ){        
          groupBySeq(tail, seq :+ node, seqMask :+ node._2, sequences)
        } else if (
                  (doesItMatch(seq.last._2,"""(text)""".r) 
                    && doesItMatch(node._2,"""(text)""".r)) ||
                  (doesItMatch(seq.last._2,"""(text)""".r) 
                    && doesItMatch(node._2,"""(title|author|date|meta)""".r) 
                    && seq.last._3 == node._3)                  
                  ){
          groupBySeq(tail, seq :+ node, seqMask, sequences)
        } else {
          groupBySeq(tail, 
                     List[Tuple3[Element,String,Int]]() :+ node, 
                     List[String]() :+ node._3.toString() :+ node._2, 
                     sequences :+ (seqMask.reduceLeft(_ + _),seq.map(s => (s._1.text(),s._2,s._1.nodeName()))))
        }
    }
  }

  def printContent(content :List[List[List[(String,String,String)]]]) = {

    for (seq <- content) {
      println("= = = = =") 
      if (seq.length > 1) {
        for(s <- seq) {
          println("")
          for (node <- s) {
            println("> " + node._2  + " " + node._3)
            println(node._1)
          }  
        }
      } else {
        for (node <- seq(0)) {
          println("> " + node._2  + " " + node._3)
          println(node._1)
        }   
      }
    }
  }

  def grabContent(dom :Document) : List[List[List[(String,String,String)]]] = {

    val nodes = cleanDom(dom.select("*").first(),List[Tuple3[Element,String,Int]]()) 

    groupBySeq(nodes, List[(Element,String,Int)](), List[String](), List[(String,List[(String,String,String)])]())

  }

  def cleanAuthor(author :String) : String = {
    pattern_clean_author.replaceAllIn(pattern_clean_date.replaceAllIn(author, ""), "").trim()
  }


  def parseDate(date: String, pattern: List[(Regex,String)] = date_match) : Option[Date] = {
    pattern match {
      case Nil => None
      case regex :: tail => 
        if (doesItMatch(date,regex._1)) {
          val format = new SimpleDateFormat(regex._2)
          Some(format.parse(regex._1.findFirstIn(date).getOrElse("")))
        } else {
          parseDate(date, tail)
        }
    }
  }

  def getMainAuthorDate(content :List[List[(String,String,String)]], author :String, date :String) : (String,String) = {
    content match {
      case Nil => (cleanAuthor(author),date)
      case seq :: tail => 
        val mask = seq.map(s => s._2).reduce(_ + _)
        var a = author
        var d = date

        if ((author == "") && doesItMatch(mask,"""(author)""".r) && doesItMatch(mask,"""text""".r)) {
          a = seq.filter(s => s._2 == "author")(0)._1
        }
        if ((date == "") && (a != "") && doesItMatch(a,pattern_clean_date)) {
          d = pattern_clean_date.findFirstIn(a).get
        } else if ((date == "") && doesItMatch(mask,"""(date)""".r) && doesItMatch(mask,"""text""".r)) {
          d = seq.filter(s => s._2 == "date")(0)._1

        }
        getMainAuthorDate(tail, a, d)
    }    
  }

  def getContent(path: String, domainName: List[String], mode: String) = {

    val dom = getDom(path,mode)

    val head = dom.select("head")

    /*
     * Get metadata from "head" div
     */
    val meta = getMetadata(dom.select("head"))

    /*
     * Get title of the page
     */

    val title = getArticleTitle(dom)

    /*
     * Clean Dom, Remove <script> & <style>
     */

    val content = grabContent(removeNodes(dom,List("head","script","style")))

    val mainAuthorDate = getMainAuthorDate(content.flatten,"", "")

    val author = mainAuthorDate._1

    val date = mainAuthorDate._2
    
  }

  def main(args: Array[String]) {

    if (args.length == 0) println(usage)
    val arglist = args.toList
    type OptionMap = Map[String, String]

    def nextOption(map: OptionMap, list: List[String]) : OptionMap = {
      list match {
        case Nil => map
        case "--mode" :: s :: tail if (s == "link") || (s == "content")  =>
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
      case "content" =>
        getContent(options("path"),splitUrl(options("path")),"url")
    }
  }
}
