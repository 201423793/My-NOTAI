Component({
  properties: {
    imagePath: { type: String, value: '' },
    isUploading: { type: Boolean, value: false }
  },

  methods: {
    onTap() {
      if (this.data.isUploading) return
      if (this.data.imagePath) {
        this.triggerEvent('preview')
      } else {
        this.triggerEvent('choose')
      }
    },

    onRemove(e) {
      this.triggerEvent('remove')
    }
  }
})
