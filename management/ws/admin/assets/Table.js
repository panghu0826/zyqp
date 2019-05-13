/**
 * Created by jaime on 2016/6/20.
 */

var Table = function(list){
    this.items = list

    this.genOrder = function(tmpList){
        for(var ind = 0; ind < tmpList.length;ind++) {
            tmpList[ind]._id = ind
        }
    }

    this.genOrder(this.items)

    this.del = function(one){
        this.items.remove(one)
    }

    this.indexOf = function(one) {
        var ind = -1
        for(ind = 0; ind < list.length; ind ++) {
            if(list[ind]._id == one._id) {
                return ind
            }
        }
        return -1
    }

    this.save = function (one) {
        if(one.isEdit) {
            var ind = this.indexOf(one)
            if(ind > -1) {
                this.items[ind] = one
            }
        } else {
            this.items.push(one)
            this.genOrder(this.items)
        }
    }

    this.changePosition = function(one, direction) {
        var list = this.items
        var ind = this.indexOf(one)
        if(ind < 0 || ind >= list.length) {
            return
        }
        ind = ind + direction;
        if(ind < 0 || ind >= list.length) return
        var tmp = list[ind - direction]
        list[ind-direction] = list[ind]
        list[ind] = tmp
        this.genOrder(this.items)
    }
}