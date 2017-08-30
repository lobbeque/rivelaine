/*
 * List of usefull patterns
 */

var pattern = {
    'title'       : /h1|h2|h3|title|titre/i,
    'author'      : /(byline|author|writtenby|p-author|pseudo|avatar|auteur|signature)/i,
    'date'        : /(date|^time|time$)/i,
    'text'        : /(content|and|article|annonce|body|column|main|shadow|discussion|post|forum|comment|commentaire|bloc|reaction)/i,
    'crap'        : /(outbrain|partenair|elementum|cross_site|clearfix|parstreet|sidebar|transparent|back_all|gpt|interstitielContainerNew|masque_fe_bottom|navbare|menu|playlist|nav|play|login|footer|bare-icon|handle|zapping|bt$|recommended|most|community|ads|nav|category|bar|popular|button|posting|playlist|disqus|extra|similar|preview|header|legends|related|remark|agegate|toolbar|outil|banner|update|combx|footer|foot|menu|modal|rss|head|shoutbox|sidebar|skyscraper|sponsor|ad-break|pagination|pager|popup)/i,
    'expNode'     : "[class*=fb-like-box],[class*=repondre],[class*=citer],[class*=follow],[class*=corps_posting],[class*=share-panel],[class*=share],[class*=facebook],[class*=twitter],[class*=googleplus],[class*=blogger],[class*=youtube],[class*=skyblog],[class*=tumblr],[class*=myspace],[class*=reddit],[class*=vkontakte],[class*=odnoklassniki],[class*=pinterest],[class*=github],[class*=linkedin],[class*=livejournal],[class*=hi5],[class*=create-comment],[class*=create-post],[class*=instagram],[class*=newsletter],[class*=create-message],[class*=like],[class*=dislike],[class*=rss],[class*=reagir],[class*=imprimer],[class*=partage],[class*=ajouter],[class*=print],[class*=react],[class*=email],[class*=save],[class*=sharetool],[class*=signup],[class*=login],[class*=reply],[class*=comment-reply],[class*=commenter]",
    'cntNode'     : "p,div,li,code,blockquote,pre,h1,h2,h3,h4,h5,h6,b,i,adress,time,span,#text",
    'textInline'  : /A|B|BIG|I|SMALL|TT|ABBR|ACRONYM|CITE|EM|STRONG|SCRIPT|SPAN|LABEL|TEXTAREA/i,
    'cleanAuthor' : /(Sujet :.*|par |by |\[ Ajouter à mes amis \]|copyright yabiladi\.com|Nom d.*|Message:.*|,|\[ MP \].*|\[ PM \]||\[ \]|\[)/g
}

module.exports = {
    pattern
};