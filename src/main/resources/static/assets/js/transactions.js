var transactionsOverview = new Vue({
  el: '#transactionstable',
  data: {
    items: [],
    received_messages: [],
    connected: false
  },
  mounted() {
    this.getTransactions()
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
      var stompConfigUrl = '/api/stomp/config'
      axios.get(stompConfigUrl)
        .then(function (response) {
          var stompconfig = response.data
          this.socket = new SockJS('http://' + stompconfig.host + ':' + stompconfig.port + stompconfig.endpoint)
          this.stompClient = Stomp.over(this.socket)
          this.stompClient.connect(
            {},
            frame => {
              this.connected = true
              console.log(frame)
              this.stompClient.subscribe(stompconfig.transactionsTopic, tick => {
                console.log(tick)
                this.received_messages.push(JSON.parse(tick.body).content)
              })
            },
            error => {
              console.log(error)
              this.connected = false
            }
          )


        })
        .catch(function (error) {
          console.log('Error fetching stomp config.' + error)
        })
    }

  }
})
