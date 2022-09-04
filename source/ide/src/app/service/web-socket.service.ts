import { Injectable } from '@angular/core'
import { Subject } from 'rxjs'
import { Router } from '@angular/router'

import { objectToMap } from '../helper'
import { TranslateService } from '@ngx-translate/core'


export class SystemObject { // TODO rename to ChameleonSystem to match backend?
  components: Map<string, any> // TODO cast to map
  nodes: Map<string, number>
  projects: Array<string>
  changingState: boolean
  projectName: string
  examples: Array<string>
}


export class Project {
  public name: string
  public files: Array<string>
  public configurations: Map<string, any>
  public nodes: Map<string, any>
  public screen: string
  public variables: Map<string, {
    'label': string,
    'datatype': string,
    'value': any
  }>
}


@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private mWebSocket: WebSocket
  public mShowSide: boolean = true
  public mSystem: SystemObject = null // new SystemObject()
  public mProject: Project
  public mNodesAsList: Array<{ groupName: string, nodeName: string, node: Node }>
  public mProject$: Subject<Project> = new Subject()


  private X = new class {
    constructor(private mWebSocketService: WebSocketService) {}


    public load(elementClass: string) {
      this.mWebSocketService.send2(elementClass, 'load')
    }

    public save(elementClass: string, value: object) {
      this.mWebSocketService.send2(elementClass, 'save', value)
    }
  

    public edit(elementClass: string, propertyName: string, value: any): void {
      this.mWebSocketService.send2(elementClass, 'edit', {
        'propertyName': propertyName,
        'value': value
      })
    }
    
  
    public create(elementClass: string, content: Object = {}): void {
      this.mWebSocketService.send2(elementClass, 'create', content)
    }
    
  
    public delete(elementClass: string): void {
      this.mWebSocketService.send2(elementClass, 'delete')
    }
  }(this)

  
  public System2 = new class {
    public mRegExp = new RegExp('^/System')
    private mElementClass: string = '/System'
  
    public load$ = new Subject<string>()
    public save$ = new Subject<Boolean>()  

    constructor(private mWebSocketService : WebSocketService) {}

    
    public load() {
      this.mWebSocketService.X.load(this.mElementClass)
    }


    public save(value: Object) {
      this.mWebSocketService.X.save(this.mElementClass, value)
    }
  }(this)

  
  public Project2 = new class {
    public mRegExp = new RegExp('^/Project:([/]*)')
  
    public create$: Subject<boolean> = new Subject<boolean>()
    public delete$: Subject<Object> = new Subject<Object>()
    public load$: Subject<string> = new Subject<string>()
    public save$: Subject<boolean> = new Subject<boolean>()
  

    constructor(private mWebSocketService : WebSocketService) {}

    
    public load(id: string) {
      this.mWebSocketService.X.load(`/Project:${id}`)
    }
  

    public create(id: string): void {
      this.mWebSocketService.X.create(`/Project:${id}`)
    }
    
  
    public save(id: string, value: any): void {
      this.mWebSocketService.X.save(`/Project:${id}`, value)
    }

    
    public delete(id: string) {
      this.mWebSocketService.X.delete(`/Project:${id}`)
    }
  }(this)

  
  public File = new class {
    public mRegExp = new RegExp('^/Project:(.*)/File:([^/]*)$')
  
    public create$: Subject<boolean> = new Subject<boolean>()
    public delete$: Subject<Object> = new Subject<Object>()
    public load$: Subject<string> = new Subject<string>()
    public save$: Subject<boolean> = new Subject<boolean>()
  

    constructor(private mWebSocketService : WebSocketService) {}

    
    public load(projectId: string, fileId: string) {
      this.mWebSocketService.X.load(`/Project:${projectId}/File:${fileId}`)
    }
  

    public create(projectId: string, fileId: string): void {
      this.mWebSocketService.X.create(`/Project:${projectId}/File:${fileId}`)
    }
    
  
    public save(projectId: string, fileId: string, value: any): void {
      this.mWebSocketService.X.save(`/Project:${projectId}/File:${fileId}`, value)
    }

    
    public delete(projectId: string, fileId: string) {
      this.mWebSocketService.X.delete(`/Project:${projectId}/File:${fileId}`)
    }
  }(this)


  public File2 = new class {
    public mRegExp = new RegExp('^/Project//(.*)///(.*)$')
  
    public create$: Subject<boolean> = new Subject<boolean>()
    public createFolder$: Subject<boolean> = new Subject<boolean>()
    public delete$: Subject<Object> = new Subject<Object>()
    public load$: Subject<string> = new Subject<string>()
    public save$: Subject<boolean> = new Subject<boolean>()
  

    constructor(private mWebSocketService : WebSocketService) {}

    
    public load(projectId: string, fileId: string) {
      this.mWebSocketService.X.load(`/Project//${projectId}///${fileId}`)
    }
  

    public create(projectId: string, fileId: string): void {
      this.mWebSocketService.X.create(`/Project//${projectId}///${fileId}`, '')
    }
  

    public createFolder(projectId: string, fileId: string): void {
      this.mWebSocketService.send2(`/Project//${projectId}///${fileId}`, 'createFolder')
    }
    
  
    public save(projectId: string, fileId: string, value: any): void {
      this.mWebSocketService.X.save(`/Project//${projectId}///${fileId}`, value)
    }

    
    public delete(projectId: string, fileId: string) {
      this.mWebSocketService.X.delete(`/Project//${projectId}///${fileId}`)
    }
  }(this)

  
  public Frontend = new class {
    public mRegExp = new RegExp('^/Project:(.*)/Frontend:([^/]*)$')
  
    public create$: Subject<boolean> = new Subject<boolean>()
    public delete$: Subject<Object> = new Subject<Object>()
    public load$: Subject<string> = new Subject<string>()
    public save$: Subject<boolean> = new Subject<boolean>()
  

    constructor(private mWebSocketService : WebSocketService) {}

    
    public load(projectId: string, fileId: string) {
      this.mWebSocketService.X.load(`/Project:${projectId}/Frontend:${fileId}`)
    }
  

    public create(projectId: string, fileId: string): void {
      this.mWebSocketService.X.create(`/Project:${projectId}/Frontend:${fileId}`)
    }
    
  
    public save(projectId: string, fileId: string, value: any): void {
      this.mWebSocketService.X.save(`/Project:${projectId}/Frontend:${fileId}`, value)
    }

    
    public delete(projectId: string, fileId: string) {
      this.mWebSocketService.X.delete(`/Project:${projectId}/Frontend:${fileId}`)
    }
  }(this)


  public Node2 = new class {
    public mRegExp = new RegExp('^/Project:(.*)/Node/Script:([^/]*)')
  
    public create$: Subject<boolean> = new Subject<boolean>()
    public save$: Subject<boolean> = new Subject<boolean>()
    public load$: Subject<Object> = new Subject<Object>()
    public delete$: Subject<boolean> = new Subject<boolean>()
  

    constructor(private mWebSocketService : WebSocketService) {}

    
    public load(projectId: string, nodeId: string = null) {
      this.mWebSocketService.X.load(`/Project:${projectId}/Node/Script:${nodeId}`)
    }
  

    public create(projectId: string, properties: Object): void {
      this.mWebSocketService.X.create(`/Project:${projectId}/Node/Script:random`, properties)
    }
    
  
    public save(projectId: string, nodeId: string, value: any): void {
      this.mWebSocketService.X.save(`/Project:${projectId}/Node/Script:${nodeId}`, value)
    }


    public delete(projectId: string, nodeId: string) {
      this.mWebSocketService.X.delete(`/Project:${projectId}/Node/Script:${nodeId}`)
    }
  }(this)


  public Component = new class {
    public mRegExp = new RegExp('^/Project:(.*)/Component:([^/]*)')
  
    public create$: Subject<boolean> = new Subject<boolean>()
    public save$: Subject<boolean> = new Subject<boolean>()
    public load$: Subject<Object> = new Subject<Object>()
    public delete$: Subject<boolean> = new Subject<boolean>()
  

    constructor(private mWebSocketService : WebSocketService) {}

    
    public load(projectId: string, nodeId: string) {
      this.mWebSocketService.X.load(`/Project:${projectId}/Component:${nodeId}`)
    }
  

    public create(projectId: string, nodeId: string): void {
      this.mWebSocketService.X.create(`/Project:${projectId}/Component:${nodeId}`)
    }
    
  
    public save(projectId: string, nodeId: string, value: any): void {
      this.mWebSocketService.X.save(`/Project:${projectId}/Component:${nodeId}`, value)
    }


    public delete(projectId: string, nodeId: string) {
      this.mWebSocketService.X.delete(`/Project:${projectId}/Component:${nodeId}`)
    }
  }(this)


  public Project = new class {
    private mId: string = 'Project' as string;

    public get$: Subject<Map<string, Map<string, { max: number, id: string, nodes: Map<string, { id: string, inputs: {}, outputs: {}}>}>>> =
    new Subject();
    public create$: Subject<boolean> = new Subject();
    public delete$: Subject<boolean> = new Subject<boolean>();


    public list$: Subject<Set<string>> = new Subject();
    public on$: Subject<boolean> = new Subject<boolean>();
    public off$: Subject<boolean> = new Subject<boolean>();
    public delete_node$: Subject<boolean> = new Subject<boolean>();
    public create_node$: Subject<boolean> = new Subject<boolean>();
    public save$: Subject<boolean> = new Subject<boolean>();
    public set_status$: Subject<boolean> = new Subject<boolean>();
    public edit_node$: Subject<boolean> = new Subject<boolean>();
    public create_configuration$: Subject<boolean> = new Subject<boolean>();
    public delete_configuration$: Subject<boolean> = new Subject<boolean>();
    public save_configuration$: Subject<boolean> = new Subject<boolean>();


    constructor(public mWebSocketService: WebSocketService) {}


    public get(pProjectName: String) {
      this.mWebSocketService.send(this.mId, 'get', {
        projectName: pProjectName
      });
    }


    public create(pProjectName: String, pGroupName: string, pNodeName: string, pX: number, pY: number, pInputCount:number = null,
      pOutputCount: number = null, pPosition = {x: 10, y:20}) {
      this.mWebSocketService.send(this.mId, 'create', {
        projectName: pProjectName,
        groupName: pGroupName,
        nodeName: pNodeName,
        inputCount: pInputCount,
        outputCount: pOutputCount,
        position: {
          x: pX,
          y: pY
        }
      });
    }


    public create_configuration(projectId: String, componentId: string) {
      this.mWebSocketService.send(this.mId, 'create_configuration', {
        'projectId': projectId,
        'componentId': componentId
      })
    }


    public delete_configuration(projectId: String, componentId: string) {
      this.mWebSocketService.send(this.mId, 'delete_configuration', {
        'projectId': projectId,
        'componentId': componentId
      })
    }


    public save_configuration(projectId: String, componentId: string, configuration: any) {
      this.mWebSocketService.send(this.mId, 'save_configuration', {
        'projectId': projectId,
        'componentId': componentId,
        'configuration': JSON.stringify(configuration)
      })
    }


    public list() {
      this.mWebSocketService.send(this.mId, 'list');
    }


    public set_status(pStatus: number) {
      this.mWebSocketService.send(this.mId, 'set_status', {
        status: pStatus
      });
    }


    public delete(pProjectName: string, pGroupName: string, pNodeName: string) {
      this.mWebSocketService.send(this.mId, 'delete', {
        projectName: pProjectName,
        groupName: pGroupName,
        nodeName: pNodeName
      });
    }


    public edit_node(pProjectName: string, pGroupName: string, pNodeName: string, pPropertyName: string, pValue: any) {
      this.mWebSocketService.send(this.mId, 'edit_node', {
        groupName: pGroupName,
        nodeName: pNodeName,
        propertyName: pPropertyName,
        value: JSON.stringify(pValue)
      });
    }


    public save(pProjectName: string, pGroupName: string, pNodeName: string, pNode: any): void {
      pNode.id = pNodeName; // workaround for now

      this.mWebSocketService.send(this.mId, 'save', {
        groupName: pGroupName,
        nodeName: pNodeName,
        node: JSON.stringify(pNode)
      });
    }
  }(this);


  public Node = new class {
    private mId: string = 'Node' as string;

    public connect$: Subject<boolean> = new Subject<boolean>();
    public disconnect$: Subject<boolean> = new Subject<boolean>();
    public edit$ = new Subject<boolean>();
    public move$ = new Subject<boolean>();


    constructor(
      public mWebSocketService: WebSocketService
      ) {}


    public connect(pProjectName: string, pSourceGroup: string, pSourceNode: string, pSourcePort: string, pTargetGroup: string, pTargetNode:
      string, pTargetPort: string): void {
      this.mWebSocketService.send(this.mId, 'connect', {
        projectName: pProjectName,
        outputNodeId: [
          pSourceGroup,
          pSourceNode,
          pSourcePort
        ],
        inputNodeId: [
          pTargetGroup,
          pTargetNode,
          pTargetPort
        ]
      });
    }


    public disconnect(pProjectName: string, pPortType: string, pGroupName: string, pNodeName: string, pPortName: string): void {
      this.mWebSocketService.send(this.mId, 'disconnect', {
        projectName: pProjectName,
        portType: pPortType,
        nodeId: [
          pGroupName,
          pNodeName,
          pPortName
        ]
      });
    }


    public edit(pProjectName: string, pGroupName: string, pNodeName: string, pPropertyName: Array<String>, pValue: any): void {
      this.mWebSocketService.send(this.mId, 'edit', {
        projectName: pProjectName,
        groupName: pGroupName,
        nodeName: pNodeName,
        propertyName: pPropertyName,
        value: JSON.stringify(pValue)
      });
    }
  }(this);
  

  constructor(
    pRouter: Router,
    pTranslateService: TranslateService
    ) {
    this.System2.load$.subscribe((pSystem) => {
      this.mSystem = JSON.parse(pSystem)

      if (this.mSystem.projectName)
        pRouter.navigate(['/', this.mSystem.projectName, 'log'], { skipLocationChange: true })
    })

    this.Project2.load$.subscribe((pProject) => {
      this.mProject = JSON.parse(pProject)

      if (!this.mProject) {
        // TODO toast the requested project doesn't exists
        this.mProject = null
        pRouter.navigate(['/'], { skipLocationChange: true })

        return
      }

      this.mProject.configurations = objectToMap(this.mProject.configurations)

      this.mNodesAsList = new Array()

      Object.keys(this.mProject.nodes).forEach(eValue => {
        Object.keys(this.mProject.nodes[eValue]).forEach(eeValue => {
          this.mNodesAsList.push({ 
            'groupName': eValue,
            'nodeName': eeValue,
            'node': this.mProject.nodes[eValue][eeValue]
          })
        })
      })

      // TODO: For one second all translations disappear. We need another way like a custom language for these
      // pTranslateService.reloadLang("en").toPromise().then(x => {
      // })
      
      for (const eVariableKey in this.mProject.variables) {
        const obj = {}
        obj[eVariableKey] = this.mProject.variables[eVariableKey].label

        //pTranslateService.setTranslation("en", obj, true)
        pTranslateService.set(eVariableKey, this.mProject.variables[eVariableKey].label)
      }
    })

    this.connect()
  }


  private onOpen(this: WebSocketService, pEvent: Event): any {
    this.System2.load()
  }


  private onMessage(this: WebSocketService, ev: MessageEvent): any {
    const lObject = JSON.parse(ev.data)
    let lProp

    if (lObject.Element.match(this.Node2.mRegExp))
      lProp = "Node2"
    else if (lObject.Element.match(this.File.mRegExp))
      lProp = "File"
    else if (lObject.Element.match(this.Frontend.mRegExp))
      lProp = "Frontend"
    else if (lObject.Element.match(this.Component.mRegExp))
      lProp = "Component"
    else if (lObject.Element.match(this.Project2.mRegExp))
      lProp = "Project2"
    else if (lObject.Element.match(this.System2.mRegExp))
      lProp = "System2"
    else if (lObject.Element.match(this.File2.mRegExp))
      lProp = "File2"
    else
      return

    this[lProp][`${lObject.Action}$`].next(lObject.Data)
  }


  private onClose(this: WebSocketService, ev: CloseEvent): any {
    switch (ev.code) {
      case 1013:
        console.error('ws limit reached');
      break;
      default:
        console.error('ws error', ev.code, ev.reason);
      break;
    }

    window.setTimeout(this.connect.bind(this), 1000);
  }


  public connect(): void {
    const lSessionId = Date.now().toString(36) + Math.random().toString(36).substr(2, 5);

    this.mWebSocket = new WebSocket(`ws://${window.location.hostname}:8081/api/${lSessionId}`);
    //this.mWebSocket = new WebSocket(`ws://192.168.1.10:8081/api/${lSessionId}`);
    this.mWebSocket.onopen = this.onOpen.bind(this);
    this.mWebSocket.onmessage = this.onMessage.bind(this);
    this.mWebSocket.onclose = this.onClose.bind(this);
  }


  public send(pGroup: string, pAction: string, pData: any = null) {
    const lRequest = {
      className: pGroup,
      actionName: pAction
    };

    if (pData != null) {
      Object.assign(lRequest, {data: pData});
    }

    this.mWebSocket.send(JSON.stringify(lRequest));
  }


  public send2(elementCass: string, action: string, data: any = null) {
    const lRequest = {
      'Element': elementCass,
      'Action': action,
    }

    if (data != null) {
      Object.assign(lRequest, {
        'Data': JSON.stringify(data)
      })
    }

    this.mWebSocket.send(JSON.stringify(lRequest))
  }


  public getStatus(): number {
    return this.mWebSocket.readyState;
  }


  public loaded(): boolean {
    return (this.mSystem != null);
  }
}
