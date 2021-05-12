var transactionsOverview = new Vue({
  el: '#transactionstable',
  data: {
    items: [],
    received_messages: [],
    connected: false,
    account: 'bar',
    balance: ''
  },
  mounted() {
    this.getTransactions()
    this.connect()
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
    }

  }
})
