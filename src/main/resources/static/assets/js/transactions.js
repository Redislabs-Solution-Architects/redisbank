var watchExampleVM = new Vue({
  el: '#table1',
  data: {
    question: '',
    answer: '',
    items: ''
  },
  mounted() {
    this.debouncedGetAnswer()
  },
  watch: {
    // whenever question changes, this function will run
    question: function (newQuestion, oldQuestion) {
      this.answer = 'Searching...'
      this.debouncedGetAnswer()
    }
  },
  created: function () {
    // _.debounce is a function provided by lodash to limit how
    // often a particularly expensive operation can be run.
    // In this case, we want to limit how often we access
    // yesno.wtf/api, waiting until the user has completely
    // finished typing before making the ajax request. To learn
    // more about the _.debounce function (and its cousin
    // _.throttle), visit: https://lodash.com/docs#debounce
    this.debouncedGetAnswer = _.debounce(this.getAnswer, 100)
  },
  methods: {
    getAnswer: function () {
      this.answer = 'Thinking...'
      var searchUrl = '/api/transactions'
      var vm = this
      axios.get(searchUrl)
        .then(function (response) {
          vm.answer = ''
          vm.items = response.data
        })
        .catch(function (error) {
          vm.answer = 'Error! Could not reach the API. ' + error
        })
    }
  }
})
