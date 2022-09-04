import { Component, HostListener, ViewChild, ElementRef, OnInit, OnDestroy } from '@angular/core';
import { map, first } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';
import { WebSocketService } from 'src/app/service/web-socket.service';
import { DialogService } from 'src/app/dialog/dialog.service';
import { DialogChannel } from 'src/app/dialog/channel/channel.dialog';
import { DialogBoolean } from 'src/app/dialog/generic/boolean/boolean.component';
import { DialogSwScript } from 'src/app/dialog/swScript/swScript.component';
import { DialogAdd } from 'src/app/dialog/add/add.component';
import { DialogCreateScript } from 'src/app/dialog/create/create_script/create_script.component';
import { deepCopy } from 'src/app/helper';
import { InfoBoxService } from 'src/app/service/infobox.service';
import { ToastService } from 'src/app/toast';
import { TranslateService } from '@ngx-translate/core';


export enum Mode {
  CONNECT = 'connect',
  EDIT = 'edit'
}


@Component({
  selector: 'chmlnProjectBackendView',
  templateUrl: './backend.view.html',
  styleUrls: ['./backend.view.scss']
})
export class BackendView {
  @HostListener('document:keydown', ['$event'])
  handleKeyDownEvent(event: KeyboardEvent) {
    const target = Array.from(document.querySelectorAll( ":hover" )).pop()

    if (target === undefined)
      return

    if (event.ctrlKey) {
      switch(target.tagName) {
        case 'rect': {
          const nodeId = target.parentElement!.parentElement!.id.split('.')
  
          switch (event.key) {
            case 'd':
              this.mDialogService.open(DialogBoolean, {
                data: {
                  header: 'Delete Node',
                  main: 'Are you sure?'
                }
              }).afterClosedSimple.pipe(first()).subscribe((ppResult) => {
                if (ppResult)
                  this.mWebSocket.Node2.delete(this.mProjectName, nodeId[1])
              })
    
              return( false )
          }
        } break
        case 'text': {
          if (!target.parentElement!.classList.contains('port'))
            return // Avoid this event if the user clicked on the label
        }
        case 'circle': {
          const nodeId = target.parentElement!.parentElement!.id.split('.')
          const portIndex = parseInt(target.parentElement!.id.split('.').pop().substr(1))
          const portType: string = target.parentElement!.classList.contains('input') ? 'input' : 'output'

          switch (event.key) {
            case 'd': {
              let lNode = this.getNodeById(nodeId[0], nodeId[1])

              if (portType == 'input') {
                if (lNode.inputs[portIndex] == null) 
                  this.mWebSocket.Project2.save(this.mProjectName, {
                    'disconnect': [`${nodeId[0]}/${nodeId[1]}`, portIndex, null, null]
                  })
                else {
                  lNode.inputs[portIndex] = null

                  this.mWebSocket.Node2.save(this.mProjectName, nodeId[1], {
                    'inputs': lNode['inputs']
                  })
                }
              } else {
                if (lNode.outputs[portIndex] == null) 
                  this.mWebSocket.Project2.save(this.mProjectName, {
                    'disconnect': [null, null, `${nodeId[0]}/${nodeId[1]}`, portIndex]
                  })
                else {
                  lNode.outputs[portIndex] = null

                  this.mWebSocket.Node2.save(this.mProjectName, nodeId[1], {
                    'outputs': lNode['outputs']
                  })
                }
              }

              return( false )
            }
          }
        } break
      }
    }
  }


  @HostListener('document:keyup', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    const target = Array.from(document.querySelectorAll( ":hover" )).pop()
    if (target === undefined)
      return

    switch(target.tagName) {
      case 'svg':
        break
      case 'rect': {
        const nodeId = target.parentElement!.parentElement!.id.split('.')

        switch(event.key) {
          case 'e':
            this.mDialogService.open(DialogSwScript, {
              data: {
                name: nodeId[1],
                node: deepCopy(this.getNodeById(nodeId[0], nodeId[1]))
              }
            }).afterClosed.subscribe((pResult) => {
              if (pResult.success) {
                this.mWebSocket.Node2.save$.pipe(first()).subscribe((ppResult) => {
                  if (ppResult)
                    this.mToastService.show({
                      type: 'success',
                      text: 'Saved'
                    })
                  else
                    this.mToastService.show({
                      type: 'error',
                      text: 'Couldn\'t save'
                    })
                })
                this.mWebSocket.Node2.save(this.mProjectName, nodeId[1], {
                  'code': pResult.value
                })
              }
            })
            break
        }
      } break
      case 'text': {
        if (!target.parentElement!.classList.contains('port'))
          return // Avoid this event if the user clicked on the label
      }
      case 'circle': {
        const nodeId = target.parentElement!.parentElement!.id.split('.')
        const portIndex = parseInt(target.parentElement!.id.split('.').pop().substr(1))
        const portType: string = target.parentElement!.classList.contains('input') ? 'input' : 'output'

        switch(event.key) {
          case 'e':            
            const ref = this.mDialogService.open(DialogChannel, {
              data: {
                type: portType,
                components: this.mWebSocket.mSystem.components,
                variables: this.mWebSocket.mProject.variables
                //selected: lNode[`${portType}s`][portIndex].channel,
                //regExp: new RegExp('^[A-Za-z.]*$')
              }
            })
      
            ref.afterClosed.subscribe((pResult) => {
              if (pResult.success) {
                if (pResult.value) {
                  // TODO check isConnected
                  if (portType == 'input')
                    this.mWebSocket.Project2.save(this.mProjectName, {
                      'disconnect': [`${nodeId[0]}/${nodeId[1]}`, portIndex, null, null]
                    })
                  else 
                    this.mWebSocket.Project2.save(this.mProjectName, {
                      'disconnect': [null, null, `${nodeId[0]}/${nodeId[1]}`, portIndex]
                    })
                }
                let node = this.getNodeById(nodeId[0], nodeId[1])
                
                let propName = `${portType}s`
                node[propName][portIndex] = pResult.value

                if (portType == 'input')
                  this.mWebSocket.Node2.save(this.mProjectName, nodeId[1], {
                    'inputs': node['inputs']
                  })
                else
                  this.mWebSocket.Node2.save(this.mProjectName, nodeId[1], {
                    'outputs': node['outputs']
                  })
              }
            })
          break
        }
      } break
    }
  }

  @ViewChild('workspace', { static: true }) mWorkspace: ElementRef<HTMLDivElement>

  private mSelected: {
    type: string,
    group: string,
    node: string,
    port: number
  } | null = null;
  private mShouldSave: boolean = false as boolean;
  private mMouseOffset = {
    x: 0,
    y: 0
  };
  private mCenter = {
    x: 0,
    y: 0
  };
  private mTransformMatrix = [1, 0, 0, 1, 0, 0];

  public mTransformMatrixAsString: string = this.mTransformMatrix.join(' ');
  public Mode = Mode;
  public mMode: Mode = Mode.EDIT;
  public mouse = {
    x: 0,
    y: 0
  };
  public mProjectName: string | null = null



  constructor(
    activatedRoute: ActivatedRoute,
    pTranslateService: TranslateService,
    private mRouter: Router,
    private mDialogService: DialogService,
    public mWebSocket: WebSocketService,
    public mInfoBoxService: InfoBoxService,
    private mToastService: ToastService) {
      activatedRoute.paramMap
        .pipe(map(params => params.get('projectName') ))
        .subscribe(projectName => {
          this.mProjectName = projectName
        })

      if (this.mWebSocket.mSystem == null)
        this.mRouter.navigateByUrl('');
  }
  
  
  public core_pointerdown(pEvent: PointerEvent, pType: string, pGroup: string,
    pNode: string): boolean {
    pEvent.stopPropagation()

    if (this.mSelected != null)
      return false

    this.mSelected = {
      type: pType,
      group: pGroup,
      node: pNode,
      port: null
    }

    this.mMouseOffset.x = pEvent.clientX - ((event.target as HTMLElement)
    .getBoundingClientRect() as DOMRect).x + 48
    this.mMouseOffset.y = pEvent.clientY - ((event.target as HTMLElement)
    .getBoundingClientRect() as DOMRect).y

    return false
  }


  public workspace_dblclick(pEvent: MouseEvent): boolean {
    if ((pEvent.target as HTMLElement).tagName != 'svg')
      return false

    const lX = Math.floor(pEvent.clientX - this.mTransformMatrix[4]);
    const lY = Math.floor(pEvent.clientY - this.mTransformMatrix[5]);

    let lGroups: Array<{ groupName: string, count: number, max: number }> = new
    Array();

    Object.keys(this.mWebSocket.mSystem.nodes).forEach((eNodeType: string) => {
      const lMax = this.mWebSocket.mSystem.nodes[eNodeType];
      let lCount: number = 0 as number;

      //if (this.mProject.nodes[eNodeType]) {
      //  lCount = Object.keys(this.mProject.nodes[eNodeType]).length;
      //}

      //if (lCount < lMax) {
        lGroups.push({
          groupName: eNodeType,
          count: lCount,
          max: lMax
        });
      //}
    });

    const ref = this.mDialogService.open(DialogAdd, {
      data: lGroups
    });

    ref.afterClosed.subscribe((pResult) => {
      if (pResult.success) {
        if (pResult.value.groupName === 'scripts') {
          const ref = this.mDialogService.open(DialogCreateScript, {});

          ref.afterClosed.subscribe((ppResult) => {
            if (ppResult.success) {
              const value: { name: string, inputCount: number, outputCount: number } = ppResult.value

              this.mWebSocket.Node2.create(this.mProjectName, {
                'position': { 'x': lX, 'y':  lY },
                'label': value.name,
                'inCount': value.inputCount,
                'outCount': value.outputCount
              })
            }
          });
        }
      }
    });

    return false; // avoid browser default action (shows google translate)
  }


  public workspace_pointerdown(event: PointerEvent): boolean {
    event.stopPropagation()

    this.mouse.x = event.clientX;
    this.mouse.y = event.clientY;

    if (this.mSelected != null)
      return false;

    this.mSelected = {
      type: 'workspace',
      group: null,
      node: null,
      port: null
    }

    return false;
  }


  public workspace_pointermove(pEvent: PointerEvent) {
    if (this.mSelected == null)
      return false

    switch (this.mSelected.type) {
      case 'core':
        const dx = pEvent.clientX - this.mouse.x;
        const dy = pEvent.clientY - this.mouse.y;

        if (dx !== 0 && dy !== 0) {
          this.mShouldSave = true;
          const lNode = this.getNodeById(this.mSelected.group, this.mSelected
            .node);

          let lX = pEvent.clientX - this.mTransformMatrix[4] - this
          .mMouseOffset.x;
          let lY = pEvent.clientY - this.mTransformMatrix[5] - this
          .mMouseOffset.y;

          if (lX <= 0)
            lX = 0
          else if (lX >= 1860)
            lX = 1860

          if (lY <= 18)
            lY = 18
          else if (lY >= 1020)
            lY = 1020

          lNode.position.x = lX;
          lNode.position.y = lY;
        }
        break;
      case 'input':
      case 'output':
        if (this.mMode != Mode.CONNECT) {
          this.mMode = Mode.CONNECT;

          // TODO if (node.isConnected)
          //this.mWebSocket.Node.disconnect(this.mProjectName, this.mSelected.type, this.mSelected.group, this.mSelected.node, `${this.mSelected.port}`)
          
          let node = this.getNodeById(this.mSelected.group, this.mSelected.node)
          
          // disconnect
          let propName = `${this.mSelected.type}s`
          node[propName][this.mSelected.port] = null
          
          if (propName == 'inputs')
            this.mWebSocket.Node2.save(this.mProjectName, this.mSelected.node, {
              'inputs': node[propName]
            })
          else
            this.mWebSocket.Node2.save(this.mProjectName, this.mSelected.node, {
              'outputs': node[propName]
            })
        }

        this.mouse.x = pEvent.clientX - this.mTransformMatrix[4];
        this.mouse.y = pEvent.clientY - this.mTransformMatrix[5];
        break;
      case 'workspace':
        if (pEvent.buttons === 1) {
          const dx = pEvent.clientX - this.mouse.x;
          const dy = pEvent.clientY - this.mouse.y;
    
          this.mTransformMatrix[4] = this.mCenter.x + dx; // TODO for zoom: ((1 - this.mTransformMatrix[0]) * this.mCenter.x) + dx;
          this.mTransformMatrix[5] = this.mCenter.y + dy; // TODO for zoom: ((1 - this.mTransformMatrix[0]) * this.mCenter.y) + dy;
    
          this.mTransformMatrixAsString = this.mTransformMatrix.join(' ');
        }
        break;
    }

    return false;
  }


  public workspace_pointerup(pEvent: PointerEvent): boolean {
    pEvent.stopPropagation();

    if (this.mSelected == null)
      return false
    
    switch (this.mSelected.type) {
      case 'workspace':
        this.mCenter.x += (pEvent.clientX - this.mouse.x);
        this.mCenter.y += (pEvent.clientY - this.mouse.y);
        break
      case 'core':
        if (this.mShouldSave == true) {
          this.mShouldSave = false
    
          const lGroup = this.mSelected.group
          const lNode = this.mSelected.node

          this.mWebSocket.mProject.nodes[lGroup][lNode].position.x = Math.floor(this.mWebSocket.mProject.nodes[lGroup][lNode].position.x)
          this.mWebSocket.mProject.nodes[lGroup][lNode].position.y = Math.floor(this.mWebSocket.mProject.nodes[lGroup][lNode].position.y)

          this.mWebSocket.Node2.save(this.mProjectName, lNode, {
            'position': this.mWebSocket.mProject.nodes[lGroup][lNode].position
          })
        }
        break
    }

    this.mSelected = null;
    this.mMode = Mode.EDIT;

    return false;
  }


  public getNodeById(pGroup: string, pNode: string): any {
    if (this.mWebSocket.mProject.nodes[pGroup]) {
      return this.mWebSocket.mProject.nodes[pGroup][pNode];
    }

    return null;
  }


  public getNodePortCount(pNode: any): number {
    let lOutputCount: number = 0 as number;
    let lInputCount: number = 0 as number;

    if (pNode.hasOwnProperty('inputs')) {
      lInputCount = pNode.inputs.length;
    }

    if (pNode.hasOwnProperty('outputs')) {
      lOutputCount = pNode.outputs.length;
    }

    return (lOutputCount > lInputCount) ? lOutputCount : lInputCount;
  }


  public formatPath(connection: Array<string>): string {
    const inNode = this.getNodeById(connection[0].split("/")[0], connection[0].split("/")[1])
    const inPort = Number.parseInt(connection[1])
    const outNode = this.getNodeById(connection[2].split("/")[0], connection[2].split("/")[1])
    const outPort = Number.parseInt(connection[3])

    return `M${outNode.position.x + 60}, ${outNode.position.y + 30 * (1 + outPort)} ${inNode.position.x},${inNode.position.y + 30 * (1 + inPort)}`
  }


  public trackByFunction(pIndex: number, pItem: any): any {
    return `${pItem.groupName}/${pItem.nodeName}`;
  }


  private clickTimeout: number = null;
  private clicked: Array<boolean> = new Array();

  public pointerDownPortHandler(event: PointerEvent, portType: string, node: any, portIndex: number) {
    event.stopPropagation()
    this.clicked[event.button] = true

    this.mouse.x = event.clientX - this.mTransformMatrix[4]
    this.mouse.y = event.clientY - this.mTransformMatrix[5]

    switch (event.pointerType) {
      case 'touch':
      case 'mouse':
        switch (event.button) {
          case 0: // left click
            if (this.clickTimeout == null) { // First Click
              this.mSelected = {
                type: portType,
                group: node.groupName,
                node: node.nodeName,
                port: portIndex
              };

              this.clickTimeout = window.setTimeout(() => { 
                if ((event.target as HTMLElement).classList.contains(('link'))) {
                  if (this.clicked[event.button]) { // Move / Connect
                  } else { /* Click */
                  }
                }
    
                this.clickTimeout = null
               }, 300)
            } else { // Second Click (dblclick)
              window.clearTimeout(this.clickTimeout)
              this.clickTimeout = null

                
            }
            break;
          case 2: // right click
            break;
          default:
        }
        break;
      default:
    }

    return false;
  }

  public pointerUpPortHandler(event: PointerEvent, targetPortType: string, targetNodeId: any, portIndex: number) {
    event.stopPropagation()
    this.clicked[event.button] = false

    switch (event.pointerType) {
      case 'touch': 
      case 'mouse':
        if (this.mSelected == null)
          break

        if (this.mSelected.type == targetPortType)
          break

        var lInputNodeId = { groupName: '', nodeName: '', portIndex: 0 }
        var lOutputNodeId = { ... lInputNodeId }
        
        switch (this.mSelected.type) {
          case 'input':
            lInputNodeId.groupName = this.mSelected.group
            lInputNodeId.nodeName = this.mSelected.node
            lInputNodeId.portIndex = this.mSelected.port
            
            lOutputNodeId.groupName = targetNodeId.groupName
            lOutputNodeId.nodeName = targetNodeId.nodeName
            lOutputNodeId.portIndex = portIndex
            break;
          case 'output':
            lOutputNodeId.groupName = this.mSelected.group
            lOutputNodeId.nodeName = this.mSelected.node
            lOutputNodeId.portIndex = this.mSelected.port

            lInputNodeId.groupName = targetNodeId.groupName
            lInputNodeId.nodeName = targetNodeId.nodeName
            lInputNodeId.portIndex = portIndex
        break;
      }

      this.mWebSocket.Project2.save(this.mProjectName, {
        'connect': [`${lInputNodeId.groupName}/${lInputNodeId.nodeName}`, `${lInputNodeId.portIndex}`, `${lOutputNodeId.groupName}/${lOutputNodeId.nodeName}`, `${lOutputNodeId.portIndex}`]
      })
    }

    this.mSelected = null;
    this.mMode = Mode.EDIT;
    //return true;
  }
}