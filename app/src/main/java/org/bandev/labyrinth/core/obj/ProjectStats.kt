package org.bandev.labyrinth.core.obj

import org.json.JSONObject

class ProjectStats(rawData: JSONObject) {

    var commits: Int = 0
    private var storageSize: Int = 0
    var repositorySize: Int = 0
    private var wikiSize: Int = 0
    private var lfsSize: Int = 0
    private var artifactsSize: Int = 0
    private var snippetSize: Int = 0
    private var packagesSize: Int = 0

    init {
        commits = rawData.getJSONObject("statistics").getInt("commit_count")
        storageSize = rawData.getJSONObject("statistics").getInt("storage_size")
        repositorySize = rawData.getJSONObject("statistics").getInt("repository_size")
        wikiSize = rawData.getJSONObject("statistics").getInt("wiki_size")
        lfsSize = rawData.getJSONObject("statistics").getInt("lfs_objects_size")
        artifactsSize = rawData.getJSONObject("statistics").getInt("job_artifacts_size")
        snippetSize = rawData.getJSONObject("statistics").getInt("snippets_size")
        packagesSize = rawData.getJSONObject("statistics").getInt("packages_size")
    }
}