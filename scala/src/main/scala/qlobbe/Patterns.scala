package qlobbe

import java.util.Locale

object Pattern {

    /*
     * Date parsing (regex,dateFormat)
     */

    val date_local = Map("en" -> Locale.ENGLISH, "mo" -> Locale.ENGLISH, "fr" -> Locale.FRENCH)

    val date_trans_dict = Map(1 -> "january", 2 -> "february", 3 -> "march", 4 -> "april", 5 -> "may", 6 -> "june", 7 -> "july", 8 -> "august", 9 -> "september", 10 -> "october", 11 -> "november", 12 -> "december")

    val date_trans_mach = List(("""يناير""".r,1),("""فبراير""".r,2),("""مارس""".r,3),
                             ("""أبريل""".r,4),("""مايو""".r,5),("""يونيو""".r,6),("""يوليوز""".r,7),("""يوليو""".r,7),("""غشت""".r,8),("""شتمبر""".r,9),("""سبتمبر""".r,9),("""أكتوبر""".r,10),("""نونبر""".r,11),
                             ("""نوفمبر""".r,11),("""ديسمبر""".r,12),("""enero""".r,1),("""febrero""".r,2),("""marzo""".r,3),("""abril""".r,4),("""mayo""".r,5),
                             ("""junio""".r,6),("""julio""".r,7),("""agosto""".r,8),("""septiembre""".r,9),("""setiembre""".r,9),("""octubre""".r,10),("""noviembre""".r,11),
                             ("""diciembre""".r,12),("""janvier""".r,1),("""fevrier""".r,2),("""février""".r,2),("""mars""".r,3),("""avril""".r,4),("""mai""".r,5),("""juin""".r,6),
                             ("""juillet""".r,7),("""aout""".r,8),("""août""".r,8),("""septembre""".r,9),("""octobre""".r,10),("""novembre""".r,11),("""decembre""".r,12),("""décembre""".r,12),("""fév""".r,2),
                             ("""aoû""".r,8),("""aou""".r,8))

    val pattern_clean_author = List(""",""".r,
                                    """par""".r,
                                    """by""".r,
                                    """\[ ajouter à mes amis \]""".r,
                                    """\[ mp \]""".r,
                                    """\[ pm \]""".r,
                                    """\[ رسالة خاصة \]\s+\[ إضافة كصديق \]""".r,
                                    """\[\s+\]""".r)

    val date_match = List(("""[0-3][0-9]\/[0-9]{2}\/[0-9]{4}( *)[0-9]{2}h[0-9]{2}""".r,"dd/MM/yyyy HH'h'mm","nop"), // 17/11/1989 HH'h'mm
                        ("""[0-1][0-9]\/[0-9]{2}\/[0-9]{4}( *)[0-9]{2}h[0-9]{2}""".r,"MM/dd/yyyy HH'h'mm","nop"), // 11/17/1989 HH'h'mm
                        ("""[0-3][0-9]\/[0-9]{2}\/[0-9]{4}( *)[0-9]{2}:[0-9]{2}""".r,"dd/MM/yyyy HH':'mm","nop"), // 17/11/1989 HH':'mm
                        ("""[0-1][0-9]\/[0-9]{2}\/[0-9]{4}( *)[0-9]{2}:[0-9]{2}""".r,"MM/dd/yyyy HH':'mm","nop"), // 11/17/1989 HH':'mm
                        ("""[0-3][0-9]\.[0-9]{2}\.[0-9]{4}( *)[0-9]{2}h[0-9]{2}""".r,"dd.MM.yyyy HH'h'mm","nop"), // 17.11.1989 HH'h'mm
                        ("""[0-1][0-9]\.[0-9]{2}\.[0-9]{4}( *)[0-9]{2}h[0-9]{2}""".r,"MM.dd.yyyy HH'h'mm","nop"), // 11.17.1989 HH'h'mm
                        ("""[0-3][0-9]\.[0-9]{2}\.[0-9]{4}( *)[0-9]{2}:[0-9]{2}""".r,"dd.MM.yyyy HH':'mm","nop"), // 17.11.1989 HH':'mm
                        ("""[0-1][0-9]\.[0-9]{2}\.[0-9]{4}( *)[0-9]{2}:[0-9]{2}""".r,"MM.dd.yyyy HH':'mm","nop"), // 11.17.1989 HH':'mm 
                        ("""[0-3][0-9]\-[0-9]{2}\-[0-9]{4}( *)[0-9]{2}h[0-9]{2}""".r,"dd-MM-yyyy HH'h'mm","nop"), // 17-11-1989 HH'h'mm
                        ("""[0-1][0-9]\-[0-9]{2}\-[0-9]{4}( *)[0-9]{2}h[0-9]{2}""".r,"MM-dd-yyyy HH'h'mm","nop"), // 11-17-1989 HH'h'mm
                        ("""[0-3][0-9]\-[0-9]{2}\-[0-9]{4}( *)[0-9]{2}:[0-9]{2}""".r,"dd-MM-yyyy HH':'mm","nop"), // 17-11-1989 HH':'mm
                        ("""[0-1][0-9]\-[0-9]{2}\-[0-9]{4}( *)[0-9]{2}:[0-9]{2}""".r,"MM-dd-yyyy HH':'mm","nop"), // 11-17-1989 HH':'mm                         
                        ("""[0-3][0-9]\-[0-9]{2}\-[0-9]{4}( *)[0-9]{2}:[0-9]{2}:[0-9]{2}""".r,"dd-MM-yyyy't'HH:mm:ss","nop"), // 17-11-1989 'T'HH:mm:ss
                        ("""[0-1][0-9]\-[0-9]{2}\-[0-9]{4}( *)[0-9]{2}:[0-9]{2}:[0-9]{2}""".r,"MM-dd-yyyy't'HH:mm:ss","nop"), // 11-17-1989 'T'HH:mm:ss
                        ("""[0-3][0-9]\/[0-9]{2}\/[0-9]{4}""".r,"dd/MM/yyyy","nop"), // 17/11/1989
                        ("""[0-1][0-9]\/[0-9]{2}\/[0-9]{4}""".r,"MM/dd/yyyy","nop"), // 11/17/1989 
                        ("""[0-3][0-9]\.[0-9]{2}\.[0-9]{4}""".r,"dd.MM.yyyy","nop"), // 17.11.1989
                        ("""[0-1][0-9]\.[0-9]{2}\.[0-9]{4}""".r,"MM.dd.yyyy","nop"), // 11.17.1989
                        ("""[0-3][0-9]\-[0-9]{2}\-[0-9]{4}""".r,"dd-MM-yyyy","nop"), // 17-11-1989
                        ("""[0-1][0-9]\-[0-9]{2}\-[0-9]{4}""".r,"MM-dd-yyyy","nop"), // 11-17-1989                         
                        ("""[0-3][0-9]\/[0-9]\/[0-9]{4}""".r,"dd/M/yyyy","nop"), // 17/1/1989
                        ("""[0-3][0-9]\.[0-9]\.[0-9]{4}""".r,"dd.M.yyyy","nop"), // 17.5.1989 
                        ("""[0-3][0-9]\-[0-9]\-[0-9]{4}""".r,"dd-M-yyyy","nop"), // 17-1-1989
                        ("""[0-9]\/[0-9]\/[0-9]{4}""".r,"d/M/yyyy","nop"), // 1/1/1989                        
                        ("""[0-9]\.[0-9]\.[0-9]{4}""".r,"d.M.yyyy","nop"), // 1.1.1989                        
                        ("""[0-9]\-[0-9]\-[0-9]{4}""".r,"M-d-yyyy","nop"), // 1-1-1989  
                        ("""[0-3][0-9]\/[0-9]{2}\/[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"dd/MM/yy HH'h'mm","nop"), // 17/11/89 HH'h'mm
                        ("""[0-1][0-9]\/[0-9]{2}\/[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"MM/dd/yy HH'h'mm","nop"), // 11/17/89 HH'h'mm
                        ("""[0-3][0-9]\/[0-9]{2}\/[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"dd/MM/yy HH':'mm","nop"), // 17/11/89 HH':'mm
                        ("""[0-1][0-9]\/[0-9]{2}\/[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"MM/dd/yy HH':'mm","nop"), // 11/17/89 HH':'mm
                        ("""[0-3][0-9]\.[0-9]{2}\.[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"dd.MM.yy HH'h'mm","nop"), // 17.11.89 HH'h'mm
                        ("""[0-1][0-9]\.[0-9]{2}\.[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"MM.dd.yy HH'h'mm","nop"), // 11.17.89 HH'h'mm
                        ("""[0-3][0-9]\.[0-9]{2}\.[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"dd.MM.yy HH':'mm","nop"), // 17.11.89 HH':'mm
                        ("""[0-1][0-9]\.[0-9]{2}\.[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"MM.dd.yy HH':'mm","nop"), // 11.17.89 HH':'mm 
                        ("""[0-3][0-9]\-[0-9]{2}\-[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"dd-MM-yy HH'h'mm","nop"), // 17-11-89 HH'h'mm
                        ("""[0-1][0-9]\-[0-9]{2}\-[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"MM-dd-yy HH'h'mm","nop"), // 11-17-89 HH'h'mm
                        ("""[0-3][0-9]\-[0-9]{2}\-[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"dd-MM-yy HH':'mm","nop"), // 17-11-89 HH':'mm
                        ("""[0-1][0-9]\-[0-9]{2}\-[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"MM-dd-yy HH':'mm","nop"), // 11-17-89 HH':'mm                        
                        ("""[0-3][0-9]\/[0-9]\/[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"dd/M/yy HH'h'mm","nop"), // 17/1/89 HH'h'mm
                        ("""[0-3][0-9]\/[0-9]\/[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"dd/M/yy HH':'mm","nop"), // 17/1/89 HH':'mm
                        ("""[0-3][0-9]\.[0-9]\.[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"dd.M.yy HH'h'mm","nop"), // 17.1.89 HH'h'mm
                        ("""[0-3][0-9]\.[0-9]\.[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"dd.M.yy HH':'mm","nop"), // 17.1.89 HH':'mm
                        ("""[0-3][0-9]\-[0-9]\-[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"dd-M-yy HH'h'mm","nop"), // 17-1-89 HH'h'mm
                        ("""[0-3][0-9]\-[0-9]\-[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"dd-M-yy HH':'mm","nop"), // 17-1-89 HH':'mm
                        ("""[0-9]\/[0-9]\/[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"d/M/yy HH'h'mm","nop"), // 7/1/89 HH'h'mm                        
                        ("""[0-9]\/[0-9]\/[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"d/M/yy HH':'mm","nop"), // 7/1/89 HH':'mm
                        ("""[0-9]\.[0-9]\.[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"d.M.yy HH'h'mm","nop"), // 7.1.89 HH'h'mm                        
                        ("""[0-9]\.[0-9]\.[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"d.M.yy HH':'mm","nop"), // 7.1.89 HH':'mm
                        ("""[0-9]\-[0-9]\-[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"d-M-yy HH'h'mm","nop"), // 7-1-89 HH'h'mm                        
                        ("""[0-9]\-[0-9]\-[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"d-M-yy HH':'mm","nop"), // 7-1-89 HH':'mm
                        ("""[0-3][0-9]\/[0-9]{2}\/[0-9]{2}""".r,"dd/MM/yy","nop"), // 17/11/89
                        ("""[0-1][0-9]\/[0-9]{2}\/[0-9]{2}""".r,"MM/dd/yy","nop"), // 11/17/89
                        ("""[0-3][0-9]\.[0-9]{2}\.[0-9]{2}""".r,"dd.MM.yy","nop"), // 17.11.89
                        ("""[0-1][0-9]\.[0-9]{2}\.[0-9]{2}""".r,"MM.dd.yy","nop"), // 11.17.89
                        ("""[0-3][0-9]\-[0-9]{2}\-[0-9]{2}""".r,"dd-MM-yy","nop"), // 17-11-89
                        ("""[0-1][0-9]\-[0-9]{2}\-[0-9]{2}""".r,"MM-dd-yy","nop"), // 11-17-89
                        ("""[0-3][0-9]\/[0-9]\/[0-9]{2}""".r,"dd/M/yy","nop"), // 17/1/89
                        ("""[0-3][0-9]\.[0-9]\.[0-9]{2}""".r,"dd.M.yy","nop"), // 17.1.89
                        ("""[0-3][0-9]\-[0-9]\-[0-9]{2}""".r,"dd-M-yy","nop"), // 17-1-89
                        ("""[0-9]\/[0-9]\/[0-9]{2}""".r,"d/M/yy","nop"), // 1/1/89
                        ("""[0-9]\.[0-9]\.[0-9]{2}""".r,"d.M.yy","nop"), // 1.1.89
                        ("""[0-9]\-[0-9]\-[0-9]{2}""".r,"d-M-yy","nop"), // 1-1-89                                                                          
                        ("""[0-3][0-9]( *)(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-9]{4}( *)[0-9]{2}h[0-9]{2}""".r,"dd MMMM yyyy HH'h'mm","en"), // 17 novembre 1989  HH'h'mm
                        ("""[0-3][0-9]( *)(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-9]{4}( *)[0-9]{2}:[0-9]{2}""".r,"dd MMMM yyyy HH':'mm","en"), // 17 novembre 1989 HH':'mm
                        ("""[0-3][0-9]\-(january|february|march|april|may|june|july|august|september|october|november|december)\-[0-9]{4}( *)[0-9]{2}h[0-9]{2}""".r,"dd-MMMM-yyyy HH'h'mm","en"), // 17-novembre-1989  HH'h'mm
                        ("""[0-3][0-9]\-(january|february|march|april|may|june|july|august|september|october|november|december)\-[0-9]{4}( *)[0-9]{2}:[0-9]{2}""".r,"dd-MMMM-yyyy HH':'mm","en"), // 17-novembre-1989 HH':'mm
                        ("""[0-9]( *)(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-9]{4}( *)[0-9]{2}h[0-9]{2}""".r,"d MMMM yyyy HH'h'mm","en"), // 1 novembre 1989 HH'h'mm                       
                        ("""[0-9]( *)(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-9]{4}( *)[0-9]{2}:[0-9]{2}""".r,"d MMMM yyyy HH':'mm","en"), // 1 novembre 1989 HH':'mm
                        ("""[0-9]\-(january|february|march|april|may|june|july|august|september|october|november|december)\-[0-9]{4}( *)[0-9]{2}h[0-9]{2}""".r,"d-MMMM-yyyy HH'h'mm","en"), // 1-novembre-1989 HH'h'mm                       
                        ("""[0-9]\-(january|february|march|april|may|june|july|august|september|october|november|december)\-[0-9]{4}( *)[0-9]{2}:[0-9]{2}""".r,"d-MMMM-yyyy HH':'mm","en"), // 1-novembre-1989 HH':'mm  
                        ("""[0-3][0-9]( *)(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-9]{4}""".r,"dd MMMM yyyy","en"), // 17 novembre 1989                        
                        ("""[0-3][0-9]\-(january|february|march|april|may|june|july|august|september|october|november|december)\-[0-9]{4}""".r,"dd-MMMM-yyyy","en"), // 17-novembre-1989   
                        ("""[0-9]( *)(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-9]{4}""".r,"d MMMM yyyy","en"), // 4 novembre 1989
                        ("""[0-3][0-9]( *)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z ]+[0-9]{4}( *)[0-9]{2}h[0-9]{2}""".r,"dd MMM yyyy HH'h'mm","en"), // 17 nov 1989 HH'h'mm
                        ("""[0-3][0-9]( *)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z ]+[0-9]{4}( *)[0-9]{2}:[0-9]{2}""".r,"dd MMM yyyy HH':'mm","en"), // 17 nov 1989 HH':'mm
                        ("""[0-9]( *)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z ]+[0-9]{4}( *)[0-9]{2}h[0-9]{2}""".r,"d MMM yyyy HH'h'mm","en"), // 7 nov 1989 HH'h'mm
                        ("""[0-9]( *)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z ]+[0-9]{4}( *)[0-9]{2}:[0-9]{2}""".r,"d MMM yyyy HH':'mm","en"), // 7 nov 1989 HH':'mm 
                        ("""[0-3][0-9]( *)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z ]+[0-9]{4}""".r,"dd MMM yyyy","en"), // 17 nov 1989 
                        ("""[0-9]( *)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\.[a-z ]+[0-9]{4}""".r,"d MMM'.' yyyy","en"), // 7 nov. 1989
                        ("""[0-9]( *)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z ]+[0-9]{4}""".r,"d MMM yyyy","en"), // 7 nov 1989
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-3][0-9], +[0-9]{4}( *)[0-9]{2}h[0-9]{2}""".r,"MMMM dd, yyyy HH'h'mm","en"),  // novembre 17, 1989 HH'h'mm                        
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-3][0-9], +[0-9]{4}( *)[0-9]{2}:[0-9]{2}""".r,"MMMM dd, yyyy HH':'mm","en"),  // novembre 17, 1989 HH':'mm                        
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-3][0-9], +[0-9]{4}""".r,"MMMM dd, yyyy","en"),  // novembre 17, 1989
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-9], +[0-9]{4}""".r,"MMMM d, yyyy","en"),  // novembre 1, 1989                        
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-3][0-9]th, [0-9]{4}( *)[0-9]{2}h[0-9]{2}""".r,"MMMM dd'th', yyyy HH'h'mm","en"), // november 17th, 1989 HH'h'mm
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-3][0-9]th, [0-9]{4}( *)[0-9]{2}:[0-9]{2}""".r,"MMMM dd'th', yyyy HH':'mm","en"), // november 17th, 1989 HH':'mm
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-3][0-9]th, [0-9]{4}""".r,"MMMM dd'th', yyyy","en"), // november 17th, 1989
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-9]th, [0-9]{4}""".r,"MMMM d'th', yyyy","en"), // november 1th, 1989
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-3][0-9]( *)[0-9]{4}( *)[0-9]{2}h[0-9]{2}""".r,"MMMM dd yyyy HH'h'mm","en"), // november 17 1989 HH'h'mm
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-3][0-9]( *)[0-9]{4}( *)[0-9]{2}:[0-9]{2}""".r,"MMMM dd yyyy HH':'mm","en"), // november 17 1989 HH':'mm
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-3][0-9]( *)[0-9]{4}""".r,"MMMM dd yyyy","en"), // november 17 1989
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-9]( *)[0-9]{4}""".r,"MMMM d yyyy","en"), // november 1 1989
                        ("""[0-3][0-9]( *)(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"dd MMMM yy HH'h'mm","en"), // 17 novembre 89  HH'h'mm
                        ("""[0-3][0-9]( *)(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"dd MMMM yy HH':'mm","en"), // 17 novembre 89 HH':'mm
                        ("""[0-3][0-9]\-(january|february|march|april|may|june|july|august|september|october|november|december)\-[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"dd-MMMM-yy HH'h'mm","en"), // 17-novembre-89  HH'h'mm
                        ("""[0-3][0-9]\-(january|february|march|april|may|june|july|august|september|october|november|december)\-[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"dd-MMMM-yy HH':'mm","en"), // 17-novembre-89 HH':'mm
                        ("""[0-9]( *)(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"d MMMM yy HH'h'mm","en"), // 1 novembre 89 HH'h'mm                       
                        ("""[0-9]( *)(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"d MMMM yy HH':'mm","en"), // 1 novembre 89 HH':'mm
                        ("""[0-9]\-(january|february|march|april|may|june|july|august|september|october|november|december)\-[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"d-MMMM-yy HH'h'mm","en"), // 1-novembre-89 HH'h'mm                       
                        ("""[0-9]\-(january|february|march|april|may|june|july|august|september|october|november|december)\-[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"d-MMMM-yy HH':'mm","en"), // 1-novembre-89 HH':'mm  
                        ("""[0-3][0-9]( *)(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-9]{2}""".r,"dd MMMM yy","en"), // 17 novembre 89                        
                        ("""[0-3][0-9]\-(january|february|march|april|may|june|july|august|september|october|november|december)\-[0-9]{2}""".r,"dd-MMMM-yy","en"), // 17-novembre-89   
                        ("""[0-9]( *)(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-9]{2}""".r,"d MMMM yy","en"), // 4 novembre 89
                        ("""[0-3][0-9]( *)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z ]+[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"dd MMM yy HH'h'mm","en"), // 17 nov 89 HH'h'mm
                        ("""[0-3][0-9]( *)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z ]+[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"dd MMM yy HH':'mm","en"), // 17 nov 89 HH':'mm
                        ("""[0-9]( *)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z ]+[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"d MMM yy HH'h'mm","en"), // 7 nov 89 HH'h'mm
                        ("""[0-9]( *)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z ]+[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"d MMM yy HH':'mm","en"), // 7 nov 89 HH':'mm 
                        ("""[0-3][0-9]( *)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z ]+[0-9]{2}""".r,"dd MMM yy","en"), // 17 nov 89 
                        ("""[0-9]( *)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\.[a-z ]+[0-9]{2}""".r,"d MMM'.'' yy","en"), // 7 nov. 89
                        ("""[0-9]( *)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z ]+[0-9]{2}""".r,"d MMM yy","en"), // 7 nov 89
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-3][0-9], +[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"MMMM dd, yy HH'h'mm","en"),  // novembre 17, 89 HH'h'mm                        
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-3][0-9], +[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"MMMM dd, yy HH':'mm","en"),  // novembre 17, 89 HH':'mm                        
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-3][0-9], +[0-9]{2}""".r,"MMMM dd, yy","en"),  // novembre 17, 89
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)[a-z ]+[0-9], +[0-9]{2}""".r,"MMMM d, yy","en"),  // novembre 1, 89                        
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-3][0-9]th, [0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"MMMM dd'th', yy HH'h'mm","en"), // november 17th, 89 HH'h'mm
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-3][0-9]th, [0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"MMMM dd'th', yy HH':'mm","en"), // november 17th, 89 HH':'mm
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-3][0-9]th, [0-9]{2}""".r,"MMMM dd'th', yy","en"), // november 17th, 89
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-9]th, [0-9]{2}""".r,"MMMM d'th', yy","en"), // november 1th, 89
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-3][0-9]\s+[0-9]{2}( *)[0-9]{2}h[0-9]{2}""".r,"MMMM dd yy HH'h'mm","en"), // november 17 89 HH'h'mm
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-3][0-9]\s+[0-9]{2}( *)[0-9]{2}:[0-9]{2}""".r,"MMMM dd yy HH':'mm","en"), // november 17 89 HH':'mm
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-3][0-9]\s+[0-9]{2}""".r,"MMMM dd yy","en"), // november 17 89
                        ("""(january|february|march|april|may|june|july|august|september|october|november|december)( *)[0-9]\s+[0-9]{2}""".r,"MMMM d yy","en") // november 1 89
                        )
}