var transactionsOverview = new Vue({
  el: '#transactionstable',
  data: {
    items: [],
    received_messages: [],
    connected: false,
    account: 'bar',
    balance: '',
    question: '',
    searchitems: []
  },
  mounted() {
    this.getTransactions()
    this.connect()
  },
  watch: {
    // whenever question changes, this function will run
    question: function (newQuestion, oldQuestion) {
      this.searchitems = []
      this.debouncedGetAnswer()
    }
  },
  created: function () {
    this.debouncedGetAnswer = _.debounce(this.getAnswer, 100)
  },
  methods: {
    getTransactions: function () {
      var transactionsUrl = '/api/transactions'
      var vm = this
      axios.get(transactionsUrl)
        .then(function (response) {
          vm.items = response.data
          vm.account = response.data[0].toAccount
          vm.balance = response.data[0].balanceAfter
        })
        .catch(function (error) {
          console.log('Error! Could not reach the API. ' + error)
        })
    },
    connect: function () {
      var vm = this
      var stompConfigUrl = '/api/config/stomp'
      axios.get(stompConfigUrl)
        .then(function (response) {
          var stompconfig = response.data
          var url = 'ws://' + stompconfig.host + ':' + stompconfig.port + stompconfig.endpoint
          this.stompClient = Stomp.client(url)

          this.stompClient.connect(
            {},
            frame => {
              this.connected = true
              this.stompClient.subscribe(stompconfig.transactionsTopic, tick => {
                var transaction = JSON.parse(tick.body)
                var transactionObject = JSON.parse(transaction.transaction)
                vm.items.unshift(transactionObject)
                vm.account = transactionObject.toAccount
                vm.balance = transactionObject.balanceAfter
              })
            },
            error => {
              console.log("Error connecting via stomp", error)
              this.connected = false
            })

        })
        .catch(function (error) {
          console.log('Error fetching stomp config.' + error)
        })
    },
    getAnswer: function () {

      var searchTerm = this.question
      if (this.question.length > 0) {
        searchTerm = searchTerm + '*'
      }

      var searchUrl = '/api/search?term=' + searchTerm
      var vm = this
      axios.get(searchUrl)
        .then(function (response) {
          vm.searchitems = response.data
        })
        .catch(function (error) {
          console.log('Error! Could not reach the API. ' + error)
        })
    }

  }
})

var areaOptions = {
  series: [
    {
      name: "Balance over time",
      data: [31, 40, 28, 51, 42, 109, 100],
    }
  ],
  chart: {
    height: 350,
    type: "area",
  },
  dataLabels: {
    enabled: false,
  },
  stroke: {
    curve: "smooth",
  },
  xaxis: {
    type: "datetime",
    categories: [
      "2018-09-19T00:00:00.000Z",
      "2018-09-19T01:30:00.000Z",
      "2018-09-19T02:30:00.000Z",
      "2018-09-19T03:30:00.000Z",
      "2018-09-19T04:30:00.000Z",
      "2018-09-19T05:30:00.000Z",
      "2018-09-19T06:30:00.000Z",
    ],
  },
  tooltip: {
    x: {
      format: "dd/MM/yy HH:mm",
    },
  },
};

var area = new ApexCharts(document.querySelector("#area"), areaOptions);
area.render();