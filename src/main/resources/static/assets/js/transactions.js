var transactionsOverview = new Vue({
  el: '#transactionstable',
  data: {
    items: [],
    received_messages: [],
    connected: false,
    account: 'bar',
    balance: '',
    question: '',
    searchitems: [],
    areaOptions : {
      series: [],
      xaxis: {
        type: "datetime",
      },
      yaxis: {
        decimalsInFloat: 2
      },
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
      noData: {
        text: 'Loading...'
      }
    },
    areaChart: '',
    pieOptions : {
      series: [1, 2, 3, 4, 5],
      chart: {
        height: 350,
        type: 'donut',
        options: {
          chart: {
            id: "chart-id"
          }
        }
      },
      labels: ['a', 'b', 'c', 'd', 'e'],
      responsive: [{
        breakpoint: 480,
        options: {
          chart: {
            width: 200,
            id: "chart-id"
          },
          legend: {
            position: 'bottom'
          }
        }
      }]
    },
    piechart:''
  },
  mounted() {
    this.areaChart = new ApexCharts(document.querySelector("#area"), this.areaOptions);
    this.areaChart.render()
    this.pieChart = new ApexCharts(document.querySelector("#chart"), this.pieOptions);
    this.pieChart.render()
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

                axios.get("/api/balance")
                  .then(function (response) {
                    vm.areaChart.updateSeries([{
                      name: 'value',
                      data: response.data
                    }])
                  })
                  .catch(function (error) {
                    console.log('Error! Could not reach the API. ' + error)
                  })

                  axios.get("/api/biggestspenders")
                  .then(function (response) {
                
                    vm.pieOptions.series = response.data.series
                    vm.pieOptions.labels = response.data.labels
                
                    console.log('pieooptons', vm.pieOptions)
                
                    vm.pieChart.destroy()
                    vm.pieChart = new ApexCharts(document.querySelector("#chart"), vm.pieOptions);                
                    vm.pieChart.render()
                
                  })
                  .catch(function (error) {
                    console.log('Error! Could not reach the API. ' + error)
                  })
                
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