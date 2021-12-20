package science.kiddd.motionwallpaper

interface MyDrawer {
    fun start(clear: Boolean)
    fun pause()
    fun destroy()
    fun offset(offset: Float)
}