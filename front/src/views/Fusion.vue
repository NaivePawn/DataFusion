<template>
<div style="height:100%">
  <fusion-side  @selectentity="selectEntity" style="height:100%;width:180px;position:fixed"></fusion-side>
    <div style="margin-left: 200px">
      <h3 style="position:relative;left:-200px">数据源</h3>
      <div class="showpanel">
        <div v-for="conn in conRewrite">
          <div class='tablecolumn' v-for="table in conn.tables" v-show="table.show" style="margin-top: 8px;">
            <span style="line-height: 50px; font-size: larger">{{table.id}}--{{table.displayName}}</span>
            <div style="display:inline" v-for="connect in selectTableProp">
              <div style="display:inline" v-for="tab in connect.data" >
                <div class="forselect"
                  draggable="true"
                  @dragstart='drag($event,connect.id,tab.tableName,column.name)'
                v-show="connect.id==table.id && tab.tableName==table.displayName && table.show"
                v-for=" column in tab.colmnuStructures">
                {{column.name}}-{{column.type}}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-button style="margin-top:10px;position:relative;left:100px;" type="primary" plain @click="addFusiondata">添加整合单元</el-button>
      <el-button style="margin-top:10px;position:relative;left:100px;" type="primary" plain @click="clearChoose">重置</el-button>
      <h3 style="position:relative;left:-200px">映射关系 </h3>
      <h3>{{selectEntityInfo.displayName}}</h3>
      <div class="showpanel">
        <div>
          <div  class='relation' @drop='drop($event,item.name)' @dragover='allowDrop($event)'  v-for="item in selectEntityInfo.properties" style="height:50px;border-bottom: 1px solid #b8b8b8">
            <span style="line-height: 50px; font-size: larger">{{item.name}}--{{item.type}}</span>
            <span class="glyphicon glyphicon-star" v-if="item.prime"></span>
          </div>
          </div>
      </div>
      <el-button style="margin-top:10px;position:relative;left:100px;" type="primary" plain @click="removerelation1">重置</el-button>
      <h3 style="position:relative;left:-200px">相交关系 </h3>
      <div class="showpanel">
        <div v-for="n in (relations.length-1)">
          <span>left</span>
          <el-cascader
            :options="join_units"
            v-model="relations[n].left"
            @change="handleChange">
          </el-cascader>
          <span>join on </span>
          <span>right</span>
          <el-cascader
            :options="join_units"
            v-model="relations[n].right"
            @change="handleChange">
          </el-cascader>
        </div>
      </div>
      <el-button style="margin-top:10px;position:relative;left:100px;" type="primary" plain @click="removerelation2">重置</el-button>
      <div>
        <el-button style="margin-top:10px;position:relative;left:100px;" type="primary" plain @click="sendmessage">提交</el-button>
      </div>
    </div>
    <div>
    <div class="md-modal modal-msg md-modal-transition" style="width:550px" v-bind:class="{'md-show':selectdata}">
      <div class="md-modal-inner">
        <div class="md-top">
          <button class="md-close" @click="clearChoose">Close</button>
        </div>
        <div class="md-content">
          <div>
            <el-row>
              <el-col :span="8">
                <el-tree
                  class="filter-tree"
                  :data="conRewrite"
                  :props="defaultProps"
                  default-expand-all
                  @node-click="handleNodeClick"
                >
                </el-tree>
              </el-col>
              <el-col :span="16">
                <div style=" width:300px;height:200px;border-left:1px solid #b8b8b8">
                  <div v-for="(tag,index) in conRewrite">
                    <el-tag
                      :key="item.displayName"
                      v-for="item in tag.tables"
                      closable
                      v-show="item.show"
                      :disable-transitions="false"
                      @close="handleClose(item)">
                      {{item.id}}-{{item.displayName}}
                    </el-tag>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
            <a href="javascript:;" class="btn-login" @click="emitSelect">提交</a>
        </div>
      </div>
    </div>
    <div class="md-overlay" v-if="selectdata" @click="selectdata=false"></div>
    </div>
</div>
</template>
<style>
  .showpanel {
    width: 800px;
    height: 200px;
    border: 2px solid #b8b8b8;
    border-radius: 2px;
    margin-top:10px;
    overflow-x:auto;
    overflow-y:auto;
    text-align: left;
  }
  .el-tag {
    margin-left: 10px;
    margin-top:10px;
  }
  .forselect{
    display:inline-block;
    /*width:70px;*/
    height:30px;
    background: #2bc4e2;
    border: 1px solid #c9d8ec;
    color:#204d74;
    margin-left: 10px;
    margin-top:10px;
    border-radius: 3px;
  }
  /*悬停效果*/
  /*.forselect :hover::after{*/
    /*content: attr(data-title);*/
    /*display: inline-block;*/
    /*padding: 10px 14px;*/
    /*border: 1px solid #ddd;*/
    /*border-radius: 5px;*/
    /*position: absolute;*/
    /*top: -50px;*/
    /*left: -30px;*/
    /*z-index:1000;*/
  /*}*/
</style>

<script>
  import {mapGetters} from 'vuex'
  import axios from 'axios'
  import fusionSide from '../components/fusion/FusionSide'
  export default{
    data(){
      return {
        selectdata:false,
        defaultProps: {
          children: 'tables',
          label: 'displayName'
        },
        displayDB:{
            id:null,
            table:"",
        },
        selectTableProp:[],      //选中的数据库表格信息
        selectEntityInfo:{
            displayName:"",
          properties:[]
        }, //选中的目标数据结构
    //   selectTableNum:0,
        relations:[{left:[],right:[]}],
        dom:null,      //选中的tag
        dragitem:{
            connectid:null,
          tablename:null,
          column:null
        },
        s2t:[],
        join_units:[],
        target_table_name:""
      }
    },
    components: {
      fusionSide
    },
    mounted(){
      this.getConnect();
    },
    computed: {
      ...mapGetters(['conns']),
      conRewrite: function () {
        var temp = []
        for (var i = 0; i < this.conns.length; i++) {
          temp[i] = {};
          temp[i].displayName = this.conns[i].displayName
          temp[i].id = this.conns[i].id
          temp[i].tables = [];
          for (var j = 0; j < this.conns[i].tables.length; j++) {
            temp[i].tables[j] = {}
            temp[i].tables[j].displayName = this.conns[i].tables[j].tableName
            temp[i].tables[j].id = this.conns[i].id
            temp[i].tables[j].show=false;
          }
        }
        //temp[0].tables[0].show=true;
        return temp;
      }

    },
    methods: {
      drag:function(event,conn,table,column){
        this.dom = event.currentTarget
        this.dragitem.connectid=conn
        this.dragitem.tablename=table
        this.dragitem.column=column
      },
      drop:function(event,entitycolumn){
        event.preventDefault();
        event.target.appendChild(this.dom);
        var  pieces2t={
            tfn:null,
          sfn:null
        }
        pieces2t.tfn=entitycolumn
        pieces2t.sfn=this.dragitem.connectid+":"+this.dragitem.tablename+":"+ this.dragitem.column;
       this.s2t.push(pieces2t);
      },
      allowDrop:function(event){
        event.preventDefault();
      },
      addFusiondata(){
          this.selectdata=true;
         $(".tablecolumn").each(function(){
        $(this).children("div:last-child").remove();
      });
      },
        getConnect(){
          this.$store.dispatch('GetConnect')
          this.$store.dispatch('GetEntity')
        },
        handleNodeClick(data){
          if(!data.tables){
           data.show =!data.show;   //传递给父组件选中的表格

          }
        },
        handleClose(tag){
            tag.show=false;
        },
      clearChoose(){
            for(let i=0;i<this.conRewrite.length;i++){
                for(let j=0;j<this.conRewrite[i].tables.length;j++){
                    this.conRewrite[i].tables[j].show=false;
                }
            }
            this.selectdata=false;
        this.s2t=[];
        this.join_units=[];
        this.relations=[{left:[],right:[]}]
      },
      emitSelect(){
        var selectDB=[];
        this.relations=[{left:[],right:[]}];
        for(let i=0;i<this.conRewrite.length;i++){
          for(let j=0;j<this.conRewrite[i].tables.length;j++){
            if(this.conRewrite[i].tables[j].show==true){
       //         this.selectTableNum+=1;
                this.relations.push({left:[],right:[]});
//              console.log( this.conRewrite[i].tables[j].id+"+"+this.conRewrite[i].tables[j].displayName)
                this.join_units.push(
                  {
                    value:this.conRewrite[i].tables[j].id+":"+this.conRewrite[i].tables[j].displayName,
                    label:this.conRewrite[i].tables[j].id+":"+this.conRewrite[i].tables[j].displayName,
                    children:[],
                  }
                );

                if(selectDB.indexOf(this.conRewrite[i].tables[j].id)<0){
                    selectDB.push(this.conRewrite[i].tables[j].id);
                   // 拿到所有需要的数据库连接信息
                }
            }
          }
        }
        for(let t=0;t<selectDB.length;t++){
           this.descriptDataBase(selectDB[t]);

        }
        this.selectdata=false;
        this.relations.splice(this.relations.length-1,1);
      },
      descriptDataBase(param){
        this.nowConn=param;
        axios.get("/kjb/cms/descriptionDataBase",{
          params:{
            "nick":param
          }
        }).then((response)=>{
          var res=response.data;
          if(res.status==1){
            var jsondata=JSON.parse(res.data);
            var conn={
                id:param,
                data:jsondata.tableStructures
            }
            this.selectTableProp.push(conn)
            for(let i=0;i<this.join_units.length;i++){
                if(this.join_units[i].label.split(":")[0]==param){
//                    console.log(this.join_units[i].label.split("+")[0])
                    for( let j=0;j<jsondata.tableStructures.length;j++){
                        if( jsondata.tableStructures[j].tableName==this.join_units[i].label.split(":")[1]){
                        //    console.log(this.join_units[i].label.split("+")[1])
                            for(let t=0;t< jsondata.tableStructures[j].colmnuStructures.length;t++){
                              this.join_units[i].children.push(
                                  {
                                    value:jsondata.tableStructures[j].colmnuStructures[t].name,
                                    label:jsondata.tableStructures[j].colmnuStructures[t].name
                                  })
                            }
                        }
                    }
                }
            }
          }
        })
      },
      selectEntity(value){
       //   console.log(value);
        this.s2t=[];
        this.selectEntityInfo.displayName=value.displayName;
        this.selectEntityInfo.properties=JSON.parse(value.properties);
        this.target_table_name=value.displayName;
      },

      removerelation1(){
         $(".relation").each(function(){
             $(this).children("div:last-child").remove();
         });
         $(".tablecolumn").each(function(){
           $(this).children("div:last-child").remove();
         });
        this.emitSelect();
        this.s2t=[];
      },
      removerelation2(){
        this.relations=[{left:[],right:[]}]
      },
      handleChange(){

      },
      sendmessage(){
        var result = {
          s2t: [],
          join_units: [],
          relations: [],
          target_table_name: ""
        }
        result.s2t = this.s2t;
        for (let i = 0; i < this.join_units.length; i++) {
                result.join_units.push(this.join_units.label);
        }
        result.relations=this.relations;
        result.target_table_name=this.target_table_name;
      }
    }
  }
</script>
