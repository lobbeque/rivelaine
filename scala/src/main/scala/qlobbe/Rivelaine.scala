package qlobbe

/*
 * qlobbe
 */

import qlobbe.Pattern

/*
 * Scala
 */
import scala.util.matching.Regex
import scala.collection.JavaConverters._
import scala.util.control.Breaks._
import scala.io.Source

/*
 * Java
 */
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io._

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
  val pattern_title      = """(title|titre|h1)""".r 
  val pattern_author     = """(byline|author|writtenby|pseudo|avatar|auteur)""".r 
  val pattern_author_no  = """(message-author|socialmedia)""".r
  val pattern_date       = """(date|time$)""".r
  val pattern_author_date= """(Date \:)""".r 
  val pattern_meta       = """(descriptif|description)""".r
  val pattern_comm       = """(comment|commentaire)""".r
  val pattern_keep       = """(content|annonce|and|article|body|column|main|shadow|discussion|post|forum|comment|bloc)""".r
  val pattern_avoid      = """(bt$|recommended|most|community|nav|category|bar|popular|button|posting|playlist|disqus|extra|similar|preview|header|legends|related|remark|agegate|toolbar|outil|banner|update|combx|footer|foot|menu|modal|rss|shoutbox|sidebar|skyscraper|sponsor|ad-break|pagination|pager|popup)""".r
  val pattern_remove     = """(sidebar|navbare|menu|playlist|nav|play|login|footer|bare-icon|handle|zapping)""".r
  val pattern_text       = List("section","h2","h3","h4","h5","h6","p","td","pre","b","#text")
  val pattern_para       = List("section","h2","h3","h4","h5","h6","p","td","tr","pre","b","br","ul","li","a","img","#text","u")  
  val pattern_noPara     = """(info)""".r
  val pattern_hidden     = """(display\:none)""".r

  /*
   * Clean patterns 
   */
  val pattern_clean_author = """(,|\[ MP \] \[ Ajouter à mes amis \]|\[ MP \]|par |by |\[ Ajouter à mes amis \]|copyright yabiladi\.com||\[ PM \]||\[ \]|\[)""".r
  val pattern_clean_date   = """(?:(?:31(\/|-|\.)(?:0?[13578]|1[02]|(?:Jan|Mar|May|Mai|Jul|Juillet|Aug|Aou|Oct|Dec)))\1|(?:(?:29|30)(\/|-|\.)(?:0?[1,3-9]|1[0-2]|(?:Jan|Mar|Apr|Avr|May|Mai|Jun|Juin|Jul|Aug|Aou|Sep|Oct|Nov|Dec))\2))(?:(?:1[6-9]|[2-9]\d)?\d{2})$|^(?:29(\/|-|\.)(?:0?2|(?:Feb))\3(?:(?:(?:1[6-9]|[2-9]\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\d|2[0-8])(\/|-|\.)(?:(?:0?[1-9]|(?:Jan|Feb|Fev|Mar|Apr|Avr|May|Mai|Jun|Juin|Jul|Juillet|Aou|Aug|Sep))|(?:1[0-2]|(?:Oct|Nov|Dec)))\4(?:(?:1[6-9]|[2-9]\d)?\d{2})""".r

  def appendMapList(map: Map[String, List[String]], k: String, v: String) : Map[String, List[String]] = {
    map + (k -> ( map(k) ::: List(v)))
  }

  /*
   *
   * regex matching
   *
   */

  def doesItMatch(word: String, pattern: Regex) : Boolean = {
    val l = pattern findFirstIn word
    ! l.isEmpty
  }

  def doesItMatchOr(word: String, patterns: List[Regex], res: Boolean = false) : Boolean = {
    if ( patterns.isEmpty )
      res
    else {
      if (!doesItMatch(word, patterns.head))
        doesItMatchOr(word, patterns.tail, res || false)
      else 
        doesItMatchOr(word, patterns.tail, true)
    }
  }

  /*
   *
   * link extraction
   *
   */

  def parseUrl(url: String) = {
    val splitedUrl = url.replace((pattern_http findFirstIn url).head + "://","")
                        .replace("www.","") 
                        .split("/")
    if ( !splitedUrl.isEmpty )
      List(splitedUrl(0),if (!splitedUrl.tail.isEmpty) splitedUrl.tail.mkString("/") else "")
    else 
      List("")
  }

  def getSiteSpace(url: String) = {
    val path = parseUrl(url)(1)
    if (path == "" || path.split("/").last == "") {
      "hub"
    } else if (path.contains("news") || path.contains("articles") || path.contains("article")) {
      "article"
    } else if (path.contains("forum") || path.contains("thread")) {
      "forum"
    } else {
      "misc"
    }
  }  

  def getDomainName(url: String) : String = {
    parseUrl(url)(0)
  }

  def getLink(content: String, domainName: String, mode: String) : Map[String, List[String]] = {
    val dom = getDom(content,mode)
    val links = Map("in_path" -> List(), "in_url" -> List(), "out_social" -> List(), "out_url" -> List())
    def grabLink(list: List[String], links: Map[String, List[String]]) : Map[String, List[String]] = {
      list match {
        case Nil => links
        case v :: tail =>
          if (doesItMatchOr(v, pattern_no_link))
            grabLink(tail, links)            
          else if (!doesItMatch(v, pattern_http))
            grabLink(tail, appendMapList(links,"in_path",v))
          else if (doesItMatch(v, domainName.r))
            grabLink(tail, appendMapList(links,"in_url",v))
          else if (doesItMatchOr(v, pattern_social))
            grabLink(tail, appendMapList(links,"out_social",v))
          else 
            grabLink(tail, appendMapList(links,"out_url",v))
      }
    }
    grabLink(getAttr(dom.select("a").toArray.toList, List[String](), "href"), links)
  }

  def getContentLink(content: String, domainName: String, mode: String) = {
    val links = getLink(content, domainName, mode)
    Map("in_path" -> links("in_path").asJava, "in_url" -> links("in_url").asJava, "out_social" -> links("out_social").asJava, "out_url" -> links("out_url").asJava).asJava
  }  

  /*
   *
   * dom tree manipulation
   *
   */  

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

  def removeNodes(dom: Document, nodes: List[String]) : Document = {
    nodes match {
      case Nil => dom
      case node :: tail => 
        dom.select(node).remove()
        removeNodes(dom,tail)
    }
  }

  def getNodeValue(dom: Elements, patterns: List[String]) : String = {
    patterns match {
      case Nil => ""
      case pattern :: tail => 
        if (doesItMatch(pattern,"meta".r)) {
          Option(dom.select(pattern).attr("content")) match {
            case None => getNodeValue(dom,tail)
            case Some(t) => t 
          }
        } else {
          Option(dom.select(pattern).text()) match {
            case None => getNodeValue(dom,tail)
            case Some(t) => t 
          }
        }       
    } 
  } 

  def getNextNodeWithLimit(node: Element, limit: Element, depthFirst: Boolean = true) : Element = {
    var nodeCp = node
    if (depthFirst && hasChild(node)) {
      node.children().first()
    } else if (hasSibling(node)) {
      node.nextElementSibling()
    } else {
      do {
        nodeCp = nodeCp.parent()
      } while (!isElementNull(nodeCp) && !hasSibling(nodeCp))
      if(hasSibling(nodeCp) && !limit.equals(nodeCp)) {
        nodeCp.nextElementSibling()
      } else {
        nodeCp
      }      
    }
  }  

  def getNextNode(node: Element, depthFirst: Boolean = true) : Element = {
    var nodeCp = node
    if (depthFirst && hasChild(node)) {
      node.children().first()
    } else if (hasSibling(node)) {
      node.nextElementSibling()
    } else {
      do {
        nodeCp = nodeCp.parent()
      } while (!isElementNull(nodeCp) && !hasSibling(nodeCp))
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

  def getNodeId(node: Element) : String = {
    (node.nodeName() + " " + node.attr("id") + " " + node.attr("class")).toLowerCase()
  }  

  def getSiblingId(node: Element, res: String) : String = {
    if (hasSibling(node)) {
      getSiblingId(node.nextElementSibling(), res + " " + getNodeId(node))
    } else {
      res
    }
  } 

  def getDirectChildrenId(node: Element) : String = {
    if (hasChild(node)) getSiblingId(node.child(0), "") else ""
  }

  def getAllChildrenId(node: Element, head: Element, id: String = "") : String = {
    if (isElementNull(node) || node.equals(head)) {
      id 
    } else {
      getAllChildrenId(getNextNodeWithLimit(node,head), head, id + getNodeId(node))
    }
  }  

  def getHiddenText(node: Node, hiddenText: String = "") : String = {
    if (isNodeNull(node) || node.nodeName != "#text") {
      hiddenText
    } else {
      getHiddenText(node.nextSibling(), hiddenText + " " + node.toString())
    }
  }

  def hasChild(node: Element) : Boolean = {
    Option(node.children().first()) match {
      case Some(c) => true
      case None => false
    }
  }

  def hasSibling(node: Element) : Boolean = {
    if (isElementNull(node)) {
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

  /*
   *
   * node tester
   *
   */  

  def isAuthor(node: Element, nodeId: String) : Boolean = {
    val childrenId = getDirectChildrenId(node)
    (node.attr("rel") == "author" || doesItMatch(nodeId, pattern_author)) && 
    node.text().length > 1 && 
    !doesItMatch(childrenId, pattern_remove) &&
    node.text().length < 100 &&
    !doesItMatch(nodeId, pattern_author_no) &&
    !doesItMatch(node.text(), pattern_clean_date)
  }

  def isStringDate(str: String) : Boolean = {
    doesItMatch(str, pattern_clean_date);
  }

  def isDate(node: Element, nodeId: String) : Boolean = {
    (doesItMatch(nodeId, pattern_date) &&
        node.text().length > 1 &&
        !doesItMatch(nodeId,pattern_social_span) &&
        node.text().length < 100) ||
    (doesItMatch(nodeId, pattern_author) &&
        doesItMatch(node.text(), pattern_clean_date))
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

  def isAvoid(node: Element, nodeId: String) : Boolean = {
    val childrenId = getDirectChildrenId(node)
    doesItMatch(nodeId,pattern_avoid) &&
    !doesItMatchOr(nodeId,List(pattern_keep, pattern_title, pattern_author, pattern_date, pattern_meta, pattern_comm)) &&
    !doesItMatchOr(childrenId,List(pattern_keep, pattern_title, pattern_author, pattern_date, pattern_meta, pattern_comm)) &&
    node.tag().toString() != "body" &&
    node.tag().toString() != "a" &&
    node.text().length < 10
  }

  def isRemove(node: Element, nodeId: String) : Boolean = {
    doesItMatch(nodeId,pattern_remove) &&
    node.text().length < 50
  }  

  def isHidden(node : Element, nodeId: String) : Boolean = {
    doesItMatch(node.attr("style"),pattern_hidden)
  }

  def isTitle(node: Element, nodeId: String) : Boolean = {
    doesItMatch(nodeId,pattern_title) &&
      !doesItMatch(nodeId,pattern_social_span) &&
      node.text().length > 1 &&
      node.tag().toString != "span"
  }

  def isElementNull(node: Element) : Boolean = {
    Option(node) match {
      case Some(n) => false
      case None => true
    }
  }

  def isNodeNull(node: Node) : Boolean = {
    Option(node) match {
      case Some(n) => false
      case None => true
    }
  }  

  def isText(node: Element) : Boolean = {
    if ((pattern_text.indexOf(node.nodeName()) != -1 && node.text().length > 1) || 
        (doesItMatch(node.attr("class"),"""com-content""".r))) {
      if (hasChild(node)) {
        val childrenId = getAllChildrenId(node.children().first(),node)
        ! doesItMatch(childrenId,pattern_author) && 
        ! doesItMatch(childrenId,pattern_date) &&
        ! doesItMatch(childrenId,pattern_title)
      } else {
        true
      } 
    } else {
      false
    }
  }

  def isHiddenText(node: Element, nodeId: String) : Boolean = {
    hasChild(node) && node.childNode(0).nodeName == "#text" && node.childNode(0).toString().length > 1 && node.ownText().length > 20 && !doesItMatch(nodeId, pattern_noPara)
  }

  def isParagraph(node: Element, nodeId: String) : Boolean = {
    !doesItMatch(nodeId,pattern_avoid) &&
    (node.tag().toString() == "div") &&
    hasChild(node) &&
    (node.text().length > 50) &&
    node.children().toArray().map(child => pattern_para.indexOf(child.asInstanceOf[Element].nodeName()) != -1).foldLeft(true)(_ && _)
  }

  /*
   *
   * date and author extraction
   *
   */

  // def cleanAuthor(author :String) : String = {
  //   pattern_clean_author.replaceAllIn(pattern_clean_date.replaceAllIn(author, ""), "").trim()
  // }

  def cleanAuthor(author: String, pattern: List[Regex] = Pattern.pattern_clean_author) : String = {
    pattern match {
      case Nil => author.trim()
      case regex :: tail => 
        if (doesItMatch(author,regex)) {
          cleanAuthor(author.replaceAll(regex.toString(),""),tail)
        } else {
          cleanAuthor(author,tail)
        }
    }
  }


  def translateDate(date: String, pattern: List[(Regex,Int)] = Pattern.date_trans_mach) : String = {
    pattern match {
      case Nil => date
      case regex :: tail => 
        if (doesItMatch(date,regex._1)) {
          translateDate(date.replaceAll(regex._1.findFirstIn(date).getOrElse(""), Pattern.date_trans_dict(regex._2)),tail)
        } else {
          translateDate(date,tail)
        }
    }
  } 

  def parseDate(date: String, pattern: List[(Regex,String,String)] = Pattern.date_match) : Option[Date] = {
    pattern match {
      case Nil => None
      case regex :: tail => 
        if (doesItMatch(date,regex._1)) {
          val format = if (regex._3 != "nop") new SimpleDateFormat(regex._2,Pattern.date_local(regex._3)) else new SimpleDateFormat(regex._2)
          try {
            Some(format.parse(regex._1.findFirstIn(date).getOrElse(""))) 
          } catch {
            case e: Exception => parseDate(date, tail)
          }
        } else {
          parseDate(date, tail)
        }
    }
  }

  def normalizeDate(date: String) : Date = {
    parseDate(translateDate(date.toLowerCase())).orNull
  }

  def normalizeAuthor(author: String) : String = {
    cleanAuthor(author.toLowerCase())
  }

  def getAuthorAndDateFromContent(content   : List[List[(String,String,String,Int)]], 
                                  author: String = "", 
                                  date  : String = ""
                                          ) : (String,String) = {
    content match {
      case Nil => (cleanAuthor(author),date)
      case seq :: tail =>   
        val mask = seq.map(s => s._2).reduce(_ + _)

        // rule to find author based on mask
        def authorMask(m: String = mask, a: String = author, seq: List[(String, String, String, Int)] = seq) : String = {
          if (a == "" && doesItMatch(m,"""(author)""".r) && doesItMatch(m,"""(text)""".r)) seq.filter(s => s._2 == "author")(0)._1 else a
        }

        // rule to find date based on mask
        def dateMask(m: String = mask, d: String = date, seq: List[(String, String, String, Int)] = seq) : String = {
          if (d == "" && doesItMatch(m,"""(date)""".r)) seq.filter(s => s._2 == "date")(0)._1 else d
        }

        getAuthorAndDateFromContent(tail, authorMask(), dateMask())

    }    
  }   

  /*
   *
   * content manipulation
   *
   */ 

  def getMainTitle(dom: Document) : String = {
    
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

  def getHeadData(dom: Elements) : Map[String,String] = {
    var meta = Map[String,String]()
    meta += "head_title" -> getNodeValue(dom,pattern_metaTitle)
    meta += "head_description" -> getNodeValue(dom,pattern_metaDesc)
    meta += "head_img" -> getNodeValue(dom,pattern_metaImg)
    meta += "head_twitter_creator" -> getNodeValue(dom,pattern_metaTwitCreator)
    meta += "head_published_time" -> getNodeValue(dom,pattern_metaPublishedTime)
    meta += "head_publisher" -> getNodeValue(dom,pattern_metaPublisher)
    meta
  }  

  def cleanDom(node: Element, selected: List[Tuple3[Element,String,Int]]) : List[Tuple3[Element,String,Int]] = {
    
    if (isElementNull(node)) {
      
      // No more node, return all selected nodes 
      
      selected
    
    } else {

      // Test the nature of the node
      
      val nodeId = getNodeId(node)

      // println(nodeId)

      if (isRemove(node,nodeId)) {
        // println("remove = " + nodeId)
        cleanDom(getNextNode(node,false),selected)

      } else if (isHidden(node,nodeId)) {
        // println("hide = " + nodeId)
        cleanDom(getNextNode(node,false),selected)            

      } else if (isAvoid(node,nodeId)) {
        // println("avoid = " + nodeId)
        cleanDom(getNextNode(node,false),selected)
      
      } else if (isTitle(node,nodeId)) {
        // println("title = " + nodeId)
        cleanDom(getNextNode(node,false),selected :+ new Tuple3(node,"title",getNbParents(node)))
      
      } else if (isAuthor(node,nodeId)) {

        val allChildrenId = getAllChildrenId(node.children().first(),node)

        if (doesItMatch(allChildrenId,"""(buzz|print)""".r)) {
          cleanDom(getNextNode(node,false),selected)
        } else if (doesItMatch(node.text(),pattern_author_date)) {
          cleanDom(getNextNode(node,false),selected :+ new Tuple3(node,"date",getNbParents(node))) 
        } else if (doesItMatch(node.text(),"""(Auteur \:)""".r)){
          cleanDom(getNextNode(getNextNode(node,false),false),selected :+ new Tuple3(getNextNode(node,false),"author",getNbParents(getNextNode(node,false))))           
        } else {
          cleanDom(getNextNode(node,false),selected :+ new Tuple3(node,"author",getNbParents(node)))          
        } 

      } else if (isDate(node,nodeId)) {
        // println("date = " + nodeId)
        cleanDom(getNextNode(node,false),selected :+ new Tuple3(node,"date",getNbParents(node)))

      } else if (isMeta(node, nodeId)) {
        // println("meta = " + nodeId)
        cleanDom(getNextNode(node,false),selected :+ new Tuple3(node,"meta",getNbParents(node)))            

      } else if (isParagraph(node, nodeId)) {   
        // println("para = " + nodeId)    
        cleanDom(getNextNode(node,false),selected :+ new Tuple3(node,"text",getNbParents(node)))

      } else if (isText(node)) {
        cleanDom(getNextNode(node,false),selected :+ new Tuple3(node.appendText(getHiddenText(node.nextSibling())),"text",getNbParents(node)))         

      } else if (isHiddenText(node, nodeId)) {       
        cleanDom(getNextNode(node,true),
                 selected :+ new Tuple3(new Element(Tag.valueOf("p"), "").text(node.text()),
                 "text",getNbParents(node)))                   
      
      } else {
        cleanDom(getNextNode(node,true),selected)
      }

    }
  }

  def groupByMask(sequences : List[(String,List[(String,String,String,Int)])],
                  mask      : String = "",
                  content   : List[List[(String,String,String,Int)]] = List[List[(String,String,String,Int)]](),
                  contents  : List[List[List[(String,String,String,Int)]]] = List[List[List[(String,String,String,Int)]]]()
                  ) : List[List[List[(String,String,String,Int)]]] = {
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
                      List[List[(String,String,String,Int)]]() :+ seq._2, 
                      contents :+ content)
        }
    } 
  }

  def groupBySeq(nodes     : List[(Element,String,Int)], 
                 seq       : List[(Element,String,Int)] = List[(Element,String,Int)](), 
                 seqMask   : List[String] = List[String](), 
                 sequences : List[(String,List[(String,String,String,Int)])] = List[(String,List[(String,String,String,Int)])]()
                 ) : List[List[List[(String,String,String,Int)]]] = {
    nodes match {  
      case Nil =>
        
        if (seq.isEmpty) {
          groupByMask(sequences)
        } else {
          groupByMask(sequences :+ (seqMask.reduceLeft(_ + _),seq.map(s => (s._1.text(),s._2,s._1.nodeName(),s._3))))
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
                     sequences :+ (seqMask.reduceLeft(_ + _),seq.map(s => (s._1.text(),s._2,s._1.nodeName(),s._3))))
        }
    }
  }

  def printContent(content :List[List[List[(String,String,String,Int)]]]) = {

    var cpp = 0
    for (seq <- content) {
      println("= = = = =") 
      if (seq.length > 1) {
        for(s <- seq) {
          println("")
          for (node <- s) {
            cpp += 1
            println("> " + node._2  + " " + node._3)
            println(node._1)
          }  
        }
      } else {
        for (node <- seq(0)) {
          cpp += 1
          println("> " + node._2  + " " + node._3)
          println(node._1)
        }   
      }
    }
    println(cpp)
  }

  def flatten(content : List[List[List[(String,String,String,Int)]]]) : List[List[Map[String,String]]] = {

    content.map(seqs => 
                seqs.map(seq =>
                         seq.map(ele => 
                                  Map("content" -> ele._1, 
                                      "type"    -> ele._2, 
                                      "markup"  -> ele._3, 
                                      "depth"   -> ele._4.toString, 
                                      "offset"  -> ((content.indexOf(seqs) + 1) * 100 + (seqs.indexOf(seq) + 1) * 10 + (seq.indexOf(ele) + 1)).toString))
                        )
               ).flatten
  }

  def grabContent(dom :Document) : List[List[List[(String,String,String,Int)]]] = {

    val nodes = cleanDom(dom.select("*").first(),List[Tuple3[Element,String,Int]]()) 

    groupBySeq(nodes)

  }

  def getContent(path: String, domainName: List[String], mode: String) = {

    val dom = getDom(path,mode)

    // val dom = getDom(path,"file")    

    val head = dom.select("head")

    // println(dom)

    /*
     * Get metadata from "head" div
     */
    val meta = getHeadData(dom.select("head"))

    /*
     * Get title of the page
     */

    val title = getMainTitle(dom)

    /*
     * Clean Dom, Remove <script> & <style>
     */

    val content = grabContent(removeNodes(dom,List("head","script","style")))

    val mainAuthorDate = getAuthorAndDateFromContent(content.flatten)

    val author = mainAuthorDate._1

    val date = mainAuthorDate._2

    // val res = meta + ("content_title" -> title) + ("content_author" -> author) + ("content_date" -> date ) + ("content" -> flatten(content))

    // println(content)

    // println(doesItMatch(content.toString(),"""(Facebook)""".r) )

    printContent(content)

    // println(flatten(content))

    // println(res.asJava)
    
  }

  def getHeaderJava(page: String) = {
    val dom : Document = getDom(page,"file")
    var res = Map[String,String]()
    res += "publisher" -> getNodeValue(dom.select("head"),pattern_metaPublisher)
    res += "published_date" -> getNodeValue(dom.select("head"),pattern_metaPublishedTime)
    res += "description" -> getNodeValue(dom.select("head"),pattern_metaDesc)
    res += "title" -> getMainTitle(dom)
    res.asJava   
  }  

  def getContentJava(page: String, mode: String = "file") = {
    
    val dom : Document = getDom(page,mode)

    val headData = getHeadData(dom.select("head"))

    val title = getMainTitle(dom)

    val content = grabContent(removeNodes(dom,List("head","script","style")))

    // val mainAuthorDate = getAuthorAndDateFromContent(content.flatten)

    // val author = mainAuthorDate._1

    // val date = mainAuthorDate._2  

    // if (doesItMatch(title,"""(Ouverture du centre culturel marocain à Bruxelles)""".r)) {
    //   val pw = new PrintWriter(new File("toto.html" ))
    //   pw.write(page)
    //   pw.close
    // }

    // println(title + " === " + author + " === " + date)
    
    val res = headData + ("content_title" -> title) + ("content" -> flatten(content).map(seq => seq.map(ele => ele.asJava).asJava).asJava)  

    res.asJava
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
        case "--test" :: s :: tail  =>
          nextOption(map ++ Map("test" -> s), tail)          
        case option :: tail => println("Unknown option "+option); println(usage); sys.exit(1)
      }
    }

    val options = nextOption(Map(),arglist)

    options("mode") match {
      case "link" =>
        println(getLink(options("path"),getDomainName(options("path")),"url"))
      case "content" =>
        getContent(options("path"),parseUrl(options("path")),"url")
        // getContent(Source.fromFile(options("path")).getLines.mkString,List(""),"file")
    }

    // if (options("test") != null) println(normalizeDate(options("test"))) 
  }
}
