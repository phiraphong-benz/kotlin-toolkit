package org.readium.r2.shared

import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONObject
import org.readium.r2.shared.metadata.BelongsTo
import org.readium.r2.shared.opds.OpdsMetadata
import java.io.Serializable
import java.net.URL
import java.util.*

class Metadata : Serializable {

    private val TAG = this::class.java.simpleName

    /// The structure used for the serialisation.
    var multilangTitle: MultilangString? = null
    /// The title of the publication.
    var title: String = ""
        get() = multilangTitle?.singleString ?: ""

    var languages: MutableList<String> = mutableListOf()
    lateinit var identifier: String
    // Contributors.
    var authors: MutableList<Contributor> = mutableListOf()
    var translators: MutableList<Contributor> = mutableListOf()
    var editors: MutableList<Contributor> = mutableListOf()
    var artists: MutableList<Contributor> = mutableListOf()
    var illustrators: MutableList<Contributor> = mutableListOf()
    var letterers: MutableList<Contributor> = mutableListOf()
    var pencilers: MutableList<Contributor> = mutableListOf()
    var colorists: MutableList<Contributor> = mutableListOf()
    var inkers: MutableList<Contributor> = mutableListOf()
    var narrators: MutableList<Contributor> = mutableListOf()
    var imprints: MutableList<Contributor> = mutableListOf()
    var direction:String = "default"
    var subjects: MutableList<Subject> = mutableListOf()
    var publishers: MutableList<Contributor> = mutableListOf()
    var contributors: MutableList<Contributor> = mutableListOf()
    var modified: Date? = null
    var publicationDate: String? = null
    var description: String? = null
    var rendition: Rendition = Rendition()
    var source: String? = null
    var epubType: MutableList<String> = mutableListOf()
    var rights: String? = null
    var rdfType: String? = null
    var otherMetadata: MutableList<MetadataItem> = mutableListOf()

    var belongsTo: BelongsTo? = null

    var duration: Int? = null

    fun titleForLang(key: String) : String?  = multilangTitle?.multiString?.get(key)

    fun writeJSON() : JSONObject{
        val obj = JSONObject()
        obj.putOpt("languages", getStringArray(languages))
        obj.putOpt("publicationDate", publicationDate)
        obj.putOpt("identifier", identifier)
        obj.putOpt("modified", modified)
        obj.putOpt("title", title)
        obj.putOpt("rendition", rendition.getJSON())
        obj.putOpt("source", source)
        obj.putOpt("rights", rights)
        tryPut(obj, subjects, "subjects")
        tryPut(obj, authors, "authors")
        tryPut(obj, translators, "translators")
        tryPut(obj, editors, "editors")
        tryPut(obj, artists, "artists")
        tryPut(obj, illustrators, "illustrators")
        tryPut(obj, letterers, "letterers")
        tryPut(obj, pencilers, "pencilers")
        tryPut(obj, colorists, "colorists")
        tryPut(obj, inkers, "inkers")
        tryPut(obj, narrators, "narrators")
        tryPut(obj, contributors, "contributors")
        tryPut(obj, publishers, "publishers")
        tryPut(obj, imprints, "imprints")
        return obj
    }

}

fun parseMetadata(metadataDict:JSONObject, feedUrl: URL?) : Metadata {
    val m = Metadata()
    if (metadataDict.has("title")) {
        m.multilangTitle = MultilangString()
        m.multilangTitle?.singleString = metadataDict.getString("title")
    }
    if (metadataDict.has("identifier")) {
        m.identifier = metadataDict.getString("identifier")
    }
    if (metadataDict.has("@type")) {
        m.rdfType = metadataDict.getString("@type")
    } else if (metadataDict.has("type")) {
        m.rdfType = metadataDict.getString("type")
    }
    if (metadataDict.has("modified")) {
        m.modified = DateTime(metadataDict.getString("modified")).toDate()
    }
    if (metadataDict.has("author")) {
        m.authors.addAll(parseContributors(metadataDict.get("author"), feedUrl))
    }
    if (metadataDict.has("translator")) {
        m.translators.addAll(parseContributors(metadataDict.get("translator"), feedUrl))
    }
    if (metadataDict.has("editor")) {
        m.editors.addAll(parseContributors(metadataDict.get("editor"), feedUrl))
    }
    if (metadataDict.has("artist")) {
        m.artists.addAll(parseContributors(metadataDict.get("artist"), feedUrl))
    }
    if (metadataDict.has("illustrator")) {
        m.illustrators.addAll(parseContributors(metadataDict.get("illustrator"), feedUrl))
    }
    if (metadataDict.has("letterer")) {
        m.letterers.addAll(parseContributors(metadataDict.get("letterer"), feedUrl))
    }
    if (metadataDict.has("penciler")) {
        m.pencilers.addAll(parseContributors(metadataDict.get("penciler"), feedUrl))
    }
    if (metadataDict.has("colorist")) {
        m.colorists.addAll(parseContributors(metadataDict.get("colorist"), feedUrl))
    }
    if (metadataDict.has("inker")) {
        m.inkers.addAll(parseContributors(metadataDict.get("inker"), feedUrl))
    }
    if (metadataDict.has("narrator")) {
        m.narrators.addAll(parseContributors(metadataDict.get("narrator"), feedUrl))
    }
    if (metadataDict.has("contributor")) {
        m.contributors.addAll(parseContributors(metadataDict.get("contributor"), feedUrl))
    }
    if (metadataDict.has("publisher")) {
        m.publishers.addAll(parseContributors(metadataDict.get("publisher"), feedUrl))
    }
    if (metadataDict.has("imprint")) {
        m.imprints.addAll(parseContributors(metadataDict.get("imprint"), feedUrl))
    }
    if (metadataDict.has("published")) {
        m.publicationDate = metadataDict.getString("published")
    }
    if (metadataDict.has("description")) {
        m.description = metadataDict.getString("description")
    }
    if (metadataDict.has("source")) {
        m.source = metadataDict.getString("source")
    }
    if (metadataDict.has("rights")) {
        m.rights = metadataDict.getString("rights")
    }
    if (metadataDict.has("subject")) {
        val subjDict= metadataDict.getJSONArray("subject")
        for (i in 0..(subjDict.length() - 1)) {
            val sub = subjDict.getJSONObject(i)
            val subject = Subject()
            if (sub.has("name")){
                subject.name = sub.getString("name")
            }
            if (sub.has("sort_as")){
                subject.sortAs = sub.getString("sort_as")
            }
            if (sub.has("scheme")){
                subject.scheme = sub.getString("scheme")
            }
            if (sub.has("code")){
                subject.code = sub.getString("code")
            }
            m.subjects.add(subject)
        }
    }
    if (metadataDict.has("belongs_to")) {
        val belongsDict = metadataDict.getJSONObject("belongs_to")
        val belongs = BelongsTo()
        if (belongsDict.has("series")){

            if (belongsDict.get("series") is JSONObject){
                m.belongsTo?.series?.add(Collection(belongsDict.getString("series")))
            } else if (belongsDict.get("series") is JSONArray) {
                val array = belongsDict.getJSONArray("series")
                for (i in 0..(array.length() - 1)) {
                    val string = array.getString(i)
                    m.belongsTo?.series?.add(Collection(string))
                }
            }
        }

        if (belongsDict.has("collection")){
            if (belongsDict.get("collection") is String){
                m.belongsTo?.collection?.add(Collection(belongsDict.getString("collection")))
            } else if (belongsDict.get("collection") is JSONObject) {
                belongs.series.add(parseCollection(belongsDict.getJSONObject("collection"), feedUrl))
            } else if (belongsDict.get("collection") is JSONArray) {
                val array = belongsDict.getJSONArray("collection")
                for (i in 0..(array.length() - 1)) {
                    val obj = array.getJSONObject(i)
                    belongs.series.add(parseCollection(obj, feedUrl))
                }
            }
        }
        m.belongsTo = belongs
    }

    if (metadataDict.has("duration")) {
        m.duration = metadataDict.getInt("duration")
    }
    if (metadataDict.has("language")) {
        if (metadataDict.get("language") is JSONObject){
            m.languages.add(metadataDict.getString("language"))
        } else if (metadataDict.get("language") is JSONArray) {
            val array = metadataDict.getJSONArray("language")
            for (i in 0..(array.length() - 1)) {
                val string = array.getString(i)
                m.languages.add(string)
            }
        }
    }

    return m
}
