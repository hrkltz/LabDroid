package io.github.herrherklotz.chameleon.x.backend.system.project

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.herrherklotz.chameleon.x.backend.system.Components
import io.github.herrherklotz.chameleon.x.extensions.IInput.in0


class Port @JsonCreator constructor() {
    @JsonIgnore
    public var mTarget: in0? = null

    @JsonIgnore
    public var mData: Any? = null
        get() {
            when (this.mLinkType) {
                "poll" -> {
                    return Components[this.mLinkId]!!.mPoolChannels[this.mLinkPort]
                }
                "stream" -> {
                    return field
                }
            }

            return null
        }

    @JsonIgnore
    public var mLinkGroup: String? = null

    @JsonIgnore
    public var mLinkId: String? = null

    @JsonIgnore
    public var mLinkPort: String? = null

    @JsonIgnore
    public var mLinkType: String = "stream" // "stream" | "poll"


    // ["node/script", "Usadasdas", "1", "stream"] this is not needed because we keep node
    // connections in the connections list
    // ["component", "battery", "Technology", "poll"]
    @set:JsonProperty("link")
    @get:JsonProperty("link")
    var link: List<String?>?
        set(_link) {
            if (_link != null) {
                mLinkGroup = _link[0]
                mLinkId = _link[1]
                mLinkPort = _link[2]
                mLinkType = _link[3]!!
            } else {
                mLinkGroup = null
                mLinkId = null
                mLinkPort = null
                mLinkType = "stream"
            }
        }
        get() =
            if ((mLinkGroup != null) && (mLinkId != null) && (mLinkPort != null))
                listOf(mLinkGroup, mLinkId, mLinkPort, mLinkType)
            else
                null
}