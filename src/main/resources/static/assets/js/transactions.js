var transactionsOverview = new Vue({
  el: '#transactionstable',
  data: {
    items: [],
    received_messages: [],
    connected: false
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
          console.log(stompconfig)
          var url = 'ws://' + stompconfig.host + ':' + stompconfig.port + stompconfig.endpoint
          console.log(url)
          this.stompClient = Stomp.client(url)
          console.log("Stomp:", this.stompClient)

          this.stompClient.connect(
            {},
            frame => {
              this.connected = true
              console.log("frame:",frame)
              this.stompClient.subscribe(stompconfig.transactionsTopic, tick => {
                console.log("tick:", tick)
                console.log("body:" , tick.body)
                var transaction = JSON.parse(tick.body)
                var transtrans = JSON.parse(transaction.transaction)
                console.log("transaciton:" , transtrans)
                console.log("id:", transtrans.id)
                vm.items.unshift(transtrans)
              })
            },
            error => {
              console.log("err", error)
              this.connected = false
            })

        })
        .catch(function (error) {
          console.log('Error fetching stomp config.' + error)
        })
    }

  }
})
