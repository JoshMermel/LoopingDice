package com.joshmermelstein.loopoverplus

// Returns wide moves according to |rowDepth| and |colDepth|.
class WideMoveFactory(override val rowDepth: Int, override val colDepth: Int) :
    WideMoveFactoryBase {

    override fun generalHelpText(): String {
        return ""
    }
}