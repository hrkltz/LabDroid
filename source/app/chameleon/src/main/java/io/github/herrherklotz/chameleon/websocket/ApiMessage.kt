package io.github.herrherklotz.chameleon.websocket


class ApiMessage(pElement: String, pAction: String, pData: String?) {
    @JvmField
    var Element: String = pElement

    @JvmField
    var Action: String = pAction

    @JvmField
    var Data: String? = pData


    // Note: This constructor is required for deserialization.
    constructor() : this("", "", null) {
        // Empty
    }
}