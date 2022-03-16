SyncTransmission
同步传输要发送到父级的同步记录的集合。
文件上传用的是MultipartHttpServletRequest
1，向父服务器执行“完全”同步（从子级角度），
    doFullSynchronize(RemoteServer parent, ReceivingSize size, Integer maxSyncRecords)
    1，更新父服务器的最后一次同步时间为now，更新父服务器的标志为正在同步
        parent.setLastSync(new Date());
        parent.setSyncInProgress(true);
    2，生成SyncTransmission对象：SyncTransmission tx = SyncUtilTransmission.createSyncTransmissionRequest(parent);
        1，将SyncTransmission序列化成xml，储存到SyncTransmission.fileOutput对象中
        2，生成的SyncTransmission.xml文件，但是这里不需要保存到本地
    3，向父服务器发送请求
        SyncTransmissionResponse initialResponse = SyncUtilTransmission.sendSyncTranssmission(parent, tx);
                1，判断sync.allow_selfsigned_certs，处理请求路径url
                2,PostMethod method = new PostMethod(url);
                    生成请求method，PostMethod
                3，在ConnectionRequest中根据同步的xml内容，封装不同的stream流（ByteArrayOutputStream，CheckedOutputStream，GZIPOutputStream）
                    ConnectionRequest request = new ConnectionRequest(content, useCompression);          
                    1，connResponse = ServerConnection.sendExportedData(server, toTransmit, isResponse);
                4，生成请求体
                    Part[] parts = { new FilePart("syncDataFile", new ByteArrayPartSource("syncDataFile", request.getBytes())),
                    			        new StringPart("username", username), new StringPart("password", password),
                    			        new StringPart("compressed", String.valueOf(useCompression)),
                    			        new StringPart("isResponse", String.valueOf(isResponse)),
                    			        new StringPart("checksum", String.valueOf(request.getChecksum())) };
                    method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
                5，发送同步数据
                    int status = client.executeMethod(method);
2，父服务器接收数据sync/import.list
    1，MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        得到MultipartHttpServletRequest
    2，MultipartFile multipartFile = multipartRequest.getFile("syncDataFile");
        得到同步数据
    3，ConnectionResponse syncResponse = new ConnectionResponse(new ByteArrayInputStream(multipartFile.getBytes()), useCompression);
       String contents = syncResponse.getResponsePayload();
        得到同步数据的string类型
    4，if (SyncConstants.CLONE_MESSAGE.equals(contents)) {
            try {
                log.info("CLONE MESSAGE RECEIVED, TRYING TO CLONE THE DB");
                File file = Context.getService(SyncService.class).generateDataFile();
                StringWriter writer = new StringWriter();
                IOUtils.copy(new FileInputStream(file), writer);
                this.sendCloneResponse(writer.toString(), response, false);
                boolean clonedDBLog = Boolean.parseBoolean(GlobalPropertyHelper.getGlobalProperty(SyncConstants.PROPERTY_SYNC_CLONED_DATABASE_LOG_ENABLED, "true"));
                if (!clonedDBLog) {
                    file.delete();
                }
            } catch (Exception ex) {
                log.warn(ex.toString());
                ex.printStackTrace();
            }
            return null;
        }
        根据判断看是否要保存同步数据生成的xml
    5，for (SyncImportRecord importRecord : priorResponse.getSyncImportRecords()) {
            Context.getService(SyncIngestService.class).processSyncImportRecord(importRecord, origin);
        }
        更新importRecord的状态
    6，SyncUtilTransmission.processSyncTransmission(st, SyncUtil.getGlobalPropetyValueAsInteger(SyncConstants.PROPERTY_NAME_MAX_RECORDS_WEB));
        处理获取所有的同步数据并进行处理
        1，for (SyncRecord record : st.getSyncRecords()) {
                try {
                    //pre-create import record in case we get exception            		
                    importRecord = new SyncImportRecord();
                    importRecord.setState(SyncRecordState.FAILED); // by default, until we know otherwise
                    importRecord.setRetryCount(record.getRetryCount());
                    importRecord.setTimestamp(record.getTimestamp());
                    importRecord.setSourceServer(origin);
                    TODO:将记录写入挂起状态，以防止其他人同时尝试处理此记录
                    //reload origin for SYNC-175
                    Integer originId = origin.getServerId();
                    //now attempt to process
                    if (log.isInfoEnabled())
                        log.info("Processing record " + record.getUuid() + " which contains "
                            + record.getContainedClassSet().toString());
                    //处理SyncRecord并创建相应的同步导入记录。
                    importRecord = Context.getService(SyncIngestService.class).processSyncRecord(record, origin);
                    origin = syncService.getRemoteServer(originId);
                } catch (SyncIngestException e) {
                    log.error("Sync error while ingesting records for server: " + origin.getNickname(), e);
                    importRecord = e.getSyncImportRecord();
                } catch (Exception e) {
                    //just report error, import record already set to failed
                    log.error("Unexpected exception while ingesting records for server: " + origin.getNickname(), e);
                    if (importRecord != null)
                        importRecord.setErrorMessage(e.getMessage());
                }
                importRecords.add(importRecord);
                //if the record update failed for any reason, do not continue on, stop now
                //adding NOT_SUPPOSED_TO_SYNC: SYNC-204.
                if (importRecord.getState() != SyncRecordState.COMMITTED
                    && importRecord.getState() != SyncRecordState.ALREADY_COMMITTED
                    && importRecord.getState() != SyncRecordState.NOT_SUPPOSED_TO_SYNC) {
                    success = false;
                    break;
                }
            }
    7，importRecord = Context.getService(SyncIngestService.class).processSyncRecord(record, origin);
        处理单条的同步数据
        1，看看这个服务器是否接受这种同步记录，如果不接受这种同步记录，设置errorMessage
        2，如果这条同步记录不存在的话就创建importRecord同步记录
        3，对于每个同步项，处理它并插入/更新数据库；将删除内容放入deletedItems集合--这些内容将在最后处理
            1，首先根据不同的类型存入不同的集合进行处理
                for (SyncItem item : record.getItems()) {
                    //如果是删除的类型
                    if (item.getState() == SyncItemState.DELETED) {
                        deletedItems.add(item);
                    } else if (item.getState() == SyncItemState.UPDATED
                        && item.getContainedType() != null
                        && ("org.openmrs.PatientIdentifier".equals(item.getContainedType().getName())
                        || "org.openmrs.PersonAttribute".equals(item.getContainedType().getName())
                        || "org.openmrs.PersonAddress".equals(item.getContainedType().getName())
                        || "org.openmrs.PersonName".equals(item.getContainedType().getName()))) {
                        //这些内容都是需要按顺序处理的，先添加到这个集合
                        treeSetItems.add(item);
                    } else if (item.getContainedType() != null && (Person.class.isAssignableFrom(item.getContainedType())
                        || Concept.class.isAssignableFrom(item.getContainedType())
                        || SyncSubclassStub.class.isAssignableFrom(item.getContainedType())
                        || SerializedObject.class.isAssignableFrom(item.getContainedType()))) {
                        //Person、Concept需要首先被处理
                        SyncImportItem importedItem = syncIngestService.processSyncItem(item, record.getOriginalUuid() + "|" + server.getUuid(), processedObjects);
                        importedItem.setKey(item.getKey());
                        importRecord.addItem(importedItem);
                        if (!importedItem.getState().equals(SyncItemState.SYNCHRONIZED)) {
                            isError = true;
                        }
                    } else {
                        //处理regular的同步记录
                        regularNewAndUpdateItems.add(item);
                    }
                }
            2，首先处理regular同步记录
                for (SyncItem item : regularNewAndUpdateItems) {
                    SyncImportItem importedItem = syncIngestService.processSyncItem(item, record.getOriginalUuid() + "|" + server.getUuid(), processedObjects);
                    importedItem.setKey(item.getKey());
                    importRecord.addItem(importedItem);
                    if (!importedItem.getState().equals(SyncItemState.SYNCHRONIZED)) {
                        isError = true;
                    }
                }
                syncIngestService.processSyncItem主要分为两部分：
                    1，processCollection：处理有关联信息的集合信息
                        1，拉取对象信息，以及关联的操作（update，recreate）；尝试使用openmrs API创建所有者的实例，并检索对与所有者关联的现有集合的引用
                        2，迭代对象序列化的条目并且执行action
                        3，使用所有者记录原始uuid最后，使用openmrs api触发所有者更新
                        1，拉出owner节点并获取owner实例：在开始处理集合条目之前，我们需要引用owner对象
                            nodes = SyncUtil.getChildNodes(incoming);
                        2，获取owner对象
                            OpenmrsObject owner = (OpenmrsObject) SyncUtil.getOpenmrsObj(ownerClassName, ownerUuid);
                        3，注意：我们不能只新建一个集合并分配给父集合：
                                若hibernate映射有级联删除，它将孤立现有集合，hibernate将抛出错误：
                                “具有cascade=“all delete orphan”的集合不再由所属实体实例“*only*”引用（如果是重新创建）；清理现有集合并重新开始
                            Method m = null;
                            m = SyncUtil.getGetterMethod(owner.getClass(), ownerCollectionPropertyName);
                            if (m == null) {
                                log.error("Cannot retrieve getter method for ownerCollectionPropertyName:" + ownerCollectionPropertyName);
                                log.error("Owner info: " + "\nownerClassName:" + ownerClassName + "\nownerCollectionPropertyName:"
                                    + ownerCollectionPropertyName + "\nownerCollectionAction:" + ownerCollectionAction + "\nownerUuid:"
                                    + ownerUuid);
                                throw new SyncIngestException(SyncConstants.ERROR_ITEM_BADXML_MISSING, null, incoming, null);
                            }
                            entries = (Set) m.invoke(owner, (Object[]) null);
                        4，两个实例，在此之后，我们可能需要创建一个新集合：
                            a)：当集合为lazy=false且为新创建时；然后向父级请求它将不会返回新的空代理，它将返回null
                            b)：特殊逻辑：
                                如果获取的所有者实例没有附加任何内容，则可以安全地创建全新的集合并将其分配给所有者，而不必担心会出现孤立删除错误。
                                如果所有者附加了内容，则我们将重新创建为delete/update；即清除现有条目，然后继续添加通过同步接收的条目。
                                这段代码基本上模仿了hibernate org.hibernate.engine.Collections.prepareCollectionForUpdate（）实现。
                            if (entries == null) {
                                if (org.hibernate.collection.internal.PersistentSortedSet.class.isAssignableFrom(collectionType)) {
                                    needsRecreate = true;
                                    entries = new TreeSet();
                                } else if (org.hibernate.collection.internal.PersistentSet.class.isAssignableFrom(collectionType)) {
                                    needsRecreate = true;
                                    entries = new HashSet();
                                }
                            }
                        5,在添加新条目之前清除现有条目
                            if ("recreate".equals(ownerCollectionAction)) {
                                entries.clear();
                            }
                        6,现在，最后处理节点，呸！！
                            for (i = 0; i < nodes.getLength(); i++) {
                                if ("entry".equals(nodes.item(i).getNodeName())) {
                                    String entryClassName = ((Element) nodes.item(i)).getAttribute("type");
                                    String entryUuid = ((Element) nodes.item(i)).getAttribute("uuid");
                                    String entryAction = ((Element) nodes.item(i)).getAttribute("action");
                                    Object entry = SyncUtil.getOpenmrsObj(entryClassName, entryUuid);
                                    // Privilege, Role, and GlobalProperty可能具有不同，不同对象的uuid
                                    if (entry == null && SyncUtil.hasNoAutomaticPrimaryKey(entryClassName)) {
                                        String key = ((Element) nodes.item(i)).getAttribute("primaryKey");
                                        entry = getOpenmrsObjectByPrimaryKey(entryClassName, key);
                                    }
                                    if (entry == null) {
                                        如果该条目不存在，则盲目忽略它，我们将尝试删除它
                                        if (!"delete".equals(entryAction)) {
                                            找不到对象：最可能的原因是数据冲突
                                            log.error("Was not able to retrieve reference to the collection entry object by uuid.");
                                            log.error("Entry info: " + "\nentryClassName: " + entryClassName + "\nentryUuid: " + entryUuid
                                                + "\nentryAction: " + entryAction);
                                            log.error("Sync record original uuid: " + originalRecordUuid);
                                            throw new SyncIngestException(SyncConstants.ERROR_ITEM_UUID_NOT_FOUND, ownerClassName + " missing "
                                                + entryClassName + "," + entryUuid, incoming, null);
                                        }
                                    } else if ("update".equals(entryAction)) {
                                        if (!OpenmrsUtil.collectionContains(entries, entry)) {
                                            entries.add(entry);
                                        }
                                    } else if ("delete".equals(entryAction)) {
                                        OpenmrsUtil.collectionContains(entries, entry);
                                        if (!entries.remove(entry)) {
                                            //在集合中找不到条目：嗯，equals的实现不好？
                                            //退回到尝试按uuid在条目中查找项
                                            OpenmrsObject toBeRemoved = null;
                                            for (Object o : entries) {
                                                if (o instanceof OpenmrsObject) {
                                                    if (entryUuid.equals(((OpenmrsObject) o).getUuid())) {
                                                        toBeRemoved = (OpenmrsObject) o;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (toBeRemoved == null) {
                                                //the item to be removed was not located in the collection: log it for reference and continue
                                                log.warn("Was not able to process collection entry delete.");
                                                log.warn("Owner info: " + "\nownerClassName:" + ownerClassName
                                                    + "\nownerCollectionPropertyName:" + ownerCollectionPropertyName
                                                    + "\nownerCollectionAction:" + ownerCollectionAction + "\nownerUuid:" + ownerUuid);
                                                log.warn("entry info: " + "\nentryClassName:" + entryClassName + "\nentryUuid:" + entryUuid);
                                                log.warn("Sync record original uuid: " + originalRecordUuid);
                                            } else {
                                                //finally, remove it from the collection
                                                entries.remove(toBeRemoved);
                                            }
                                        }
                                    } else {
                                        log.error("Unknown collection entry action, action was: " + entryAction);
                                        throw new SyncIngestException(SyncConstants.ERROR_ITEM_NOT_COMMITTED, ownerClassName, incoming, null);
                                    }
                                }
                            }
                        7，将原始uuid传递给拦截器：这将防止将更改发送回原始服务器。
                            HibernateSyncInterceptor.setOriginalRecordUuid(originalRecordUuid);
                        8，如果重新创建集合，则将其分配回所有者
                            if (needsRecreate) {
                          	    SyncUtil.setProperty(owner, ownerCollectionPropertyName, entries);
                          	}
                        9，最后，触发更新。至少不需要为集合处理预提交操作
                            SyncUtil.updateOpenmrsObject(owner, ownerClassName, ownerUuid);
                                if (o == null) {
                                    log.warn("Will not update OpenMRS object that is NULL");
                                    return;
                                }
                                if ("org.openmrs.Obs".equals(className)) {
                                    // if an obs comes through with a non-null voidReason, make sure we change it back to using a PK
                                    Obs obs = (Obs) o;
                                    String voidReason = obs.getVoidReason();
                                    if (StringUtils.hasLength(voidReason)) {
                                        int start = voidReason.lastIndexOf(" ") + 1; // assumes uuids don't have spaces 
                                        int end = voidReason.length() - 1;
                                        try {
                                            String otherObsUuid = voidReason.substring(start, end);
                                            OpenmrsObject openmrsObject = getOpenmrsObj("org.openmrs.Obs", otherObsUuid);
                                            Integer obsId = openmrsObject.getId();
                                            obs.setVoidReason(voidReason.substring(0, start) + obsId + ")");
                                        } catch (Exception e) {
                                            log.trace("unable to get a uuid from obs voidReason. obs uuid: " + uuid, e);
                                        }
                                    }
                                } else if ("org.openmrs.api.db.LoginCredential".equals(className)) {
                                    LoginCredential login = (LoginCredential) o;
                                    OpenmrsObject openmrsObject = getOpenmrsObj("org.openmrs.User", login.getUuid());
                                    Integer userId = openmrsObject.getId();
                                    login.setUserId(userId);
                                }
                                //now do the save; see method comments to see why SyncSubclassStub is handled differently
                                if ("org.openmrs.sync.SyncSubclassStub".equals(className)) {
                                    SyncSubclassStub stub = (SyncSubclassStub) o;
                                    Context.getService(SyncIngestService.class).processSyncSubclassStub(stub);
                                } else {
                                    Context.getService(SyncService.class).saveOrUpdate(o);
                                }
                                return;

数据同步使用netty
长连接模式：
    服务器性能还不错，客户端数量比较少，对"报文数据"实时性要求比较高所以使用的是长连接模式
    1，创建连接
    2，发送心跳包
    3，一段时间没有接收到心跳包或者用户主动关闭之后关闭连接
长连接模式下连接的维护工作怎么做？
    客户端间隔5分钟，向服务器发送一次心跳报文，如果服务器回应正常，那就无所谓，如果不回应，一般就直接断开连接，然后重新向服务器申请新的连接
    实现：
        1，ChannelInitializer加入IdleStateHandler，每4秒检查一下是否有写事件，如果没有就触发HeartBeatClientHandler中的userEventTriggered向服务端发送心跳
        2，ChannelInitializer加入ReconnectHandler，当channel失去连接的时候就触发ReconnectHandler中的channelInactive，然后这里面定义好重连策略，如果没有超过最大连接数，就重新连接
通过ssl进行认证
    Netty SSL安全特性：Netty通过SSLHandler提供了SSL的支持，它支持的SSL协议类型包括：SSL V2、SSL V3和TLS
    SSL单项认证：客户端只验证服务端的合法性，服务端不验证客户端
    1，利用JDK的keytool工具生成自签名证书
        （1）生成Netty服务器私钥和证书仓库：
        `keytool -genkey -alias securechat -keysize 2048 -validity 365 -keyalg RSA -dname "CN=localhost" -keypass sNetty -storepass sNetty -keystore sChat.jks`
        （2）生成Netty服务端自签名证书：
        `keytool -genkey -alias smcc -keysize 2048 -validity 365 -keyalg RSA -dname "CN=localhost" -keypass cNetty -storepass cNetty -keystore cChat.jks`
        （3）生成客户端的密钥对和证书仓库，用于服务端的证书保存到客户端的授信证书仓库中：
        `keytool -export -alias securechat -keystore sChat.jks -storepass sNetty -file sChat.cer`
        （4）将Netty服务端的证书导入到客户端的证书仓库中：
        `keytool -import -trustcacerts -alias securechat -file sChat.cer -storepass cNetty -keystore cChat.jks`
    2，核心代码
        详见/Users/yangshuai/project/shuai/netty/src/main/java/com/shuai/netty/ssl
    3，认证原理
        （1）SSL客户端向服务端传送客户端SSL协议的版本号、支持的加密算法种类产生的随机数等信息
        （2）服务端返回握手答应，向客户端传送确认SSL协议的版本号、支持的加密算法种类产生的随机数等信息
        （3）服务端向客户端发送自己的公钥；
        （4）客户端对服务端的证书进行认证，服务端的合法性校验包含：证书是否过期、发行服务器证书的CA是否可靠、发行者证书的公钥能否正确解开服务器的“发行者的数组签名”、服务器证书上的域名是否和服务器的实际域名相匹配等；
        （5）客户端随机生成一个用于后面通讯的“对称密码”，然后用服务端的公钥对其加密，将加密后的“预主密码”传给服务端；
        （6）服务端将自己的私钥解开加密的“预主密码”，然后执行一系列步骤来产生主密码；
        （7）客户端向服务端发出信息，指明后面的数据通讯将使用主密码为对称密钥，同时通知服务器客户端的握手过程结束；
        （8）服务端向客户端发出信息，指明后面的数据通讯将使用主密码为对称密钥，同时通知客户端服务器端的握手过程结束；
        （9）SSL的握手部分结束，SSL安全通道建立，客户端和服务端开始使用相同的对称密钥对数据进行加密，然后通过Socket进行传输
数据存储形式
    利用hibernate的EmptyInterceptor，在增删改的时候就会进行拦截，然后生成同步数据
    同步数据是一张表名字叫做SyncRecord， 主要字段有:
    1, state
        /** initial state of a sync record */
        NEW,
        /** sync record is being sent to target sync source however it's transmission to the target sync source has not been confirmed */
        PENDING_SEND,
        /** the record has been successful transmitted to the target source, note it may not yet be committed */
        SENT,
        /** attempted send failed */
        SEND_FAILED,
        /** the record was successfully committed at target source (the source server hasn't been notified yet) */
        COMMITTED,
        /** This record has been committed and the source server has been notified of this **/
        COMMITTED_AND_CONFIRMATION_SENT,
        /** the record reached the failed state during ingest: will retry next time */
        FAILED,
        /** the record reached the final failed state: max retry attempt was reached, no more retries will be attempted */
        FAILED_AND_STOPPED,
        /** we are trying again to send this record */
        SENT_AGAIN,
        /** this record has already been committed */
        ALREADY_COMMITTED,
        /** this record is set not to sync with the referenced server */
        NOT_SUPPOSED_TO_SYNC,
        /** record was sent to server, but server does not accept this type of record for sync'ing */
        REJECTED;
    2, contained_classes
        这次同步记录所包含的类
    3, payload:
            ``
            <?xml version="1.0" encoding="utf-8"?>
            <items>
              <!-- 这个item的状态时new，说明是一个新建操作，类型是Obs，意味着新建一个obs。外键通过uuid来关联 -->
              <SyncItem containedType="org.openmrs.Obs" key="fb92fc8c-9b9d-4234-ada4-7c00cf5568e4" state="NEW">
                <content>
                  <org.openmrs.Obs>
                    <creator type="org.openmrs.User">eceaf821-4002-4d5d-884d-b8e7ee8be1b4</creator>
                    <obsDatetime type="timestamp">2022-02-25T14:20:01.198+0800</obsDatetime>
                    <dateCreated type="timestamp">2022-02-25T14:20:01.240+0800</dateCreated>
                    <person type="org.openmrs.Person">eb532006-b0dc-4210-9635-e8a18db16ebd</person>
                    <concept type="org.openmrs.Concept">b16f9660-b32f-4fba-9b05-98bf375aa54f</concept>
                    <voided type="boolean">false</voided>
                    <location type="org.openmrs.Location">15e3410c-ca6b-49fd-a090-140fe3d285d3</location>
                    <valueCoded type="org.openmrs.Concept">615cf952-e78a-43b6-8359-90a4c3df06d0</valueCoded>
                    <encounter type="org.openmrs.Encounter">d70a1e66-2b7d-4cff-a0c3-f272e73fee53</encounter>
                    <uuid type="string">fb92fc8c-9b9d-4234-ada4-7c00cf5568e4</uuid>
                  </org.openmrs.Obs>
                </content>
              </SyncItem>
              <SyncItem containedType="org.openmrs.Obs" key="24402884-31db-4bfc-9563-db3557307088" state="NEW">
                <content>
                  <org.openmrs.Obs>
                    <creator type="org.openmrs.User">eceaf821-4002-4d5d-884d-b8e7ee8be1b4</creator>
                    <obsDatetime type="timestamp">2022-02-25T14:20:01.206+0800</obsDatetime>
                    <dateCreated type="timestamp">2022-02-25T14:20:01.246+0800</dateCreated>
                    <person type="org.openmrs.Person">eb532006-b0dc-4210-9635-e8a18db16ebd</person>
                    <concept type="org.openmrs.Concept">0ad9b5d1-c0c2-490d-8ec9-2139eadbc1cf</concept>
                    <voided type="boolean">false</voided>
                    <location type="org.openmrs.Location">15e3410c-ca6b-49fd-a090-140fe3d285d3</location>
                    <valueCoded type="org.openmrs.Concept">904b0fb3-67a7-4301-a70c-e08e098edf98</valueCoded>
                    <encounter type="org.openmrs.Encounter">d70a1e66-2b7d-4cff-a0c3-f272e73fee53</encounter>
                    <uuid type="string">24402884-31db-4bfc-9563-db3557307088</uuid>
                  </org.openmrs.Obs>
                </content>
              </SyncItem>
              <!-- 这个item的状态时update，说明是一个更新操作操作，类型是Encounter，意味着更新这个encounter。外键通过uuid来关联 -->
              <SyncItem containedType="org.openmrs.Encounter" key="d70a1e66-2b7d-4cff-a0c3-f272e73fee53" state="UPDATED">
                <content>
                  <org.openmrs.Encounter>
                    <dateChanged type="timestamp">2022-02-25T14:20:01.306+0800</dateChanged>
                    <encounterVerification type="org.openmrs.EncounterVerification">e0c2cc2c-e3eb-43a4-b2d1-53e8107b8712</encounterVerification>
                    <creator type="org.openmrs.User">eceaf821-4002-4d5d-884d-b8e7ee8be1b4</creator>
                    <encounterTargetSdv type="org.openmrs.EncounterTargetSdv">ec20eb81-8262-422a-bc0b-57f899ff276c</encounterTargetSdv>
                    <encounterDatetime type="timestamp">2022-01-13T00:00:00.000+0800</encounterDatetime>
                    <uuid type="string">d70a1e66-2b7d-4cff-a0c3-f272e73fee53</uuid>
                    <dateCreated type="timestamp">2022-02-25T14:19:19.000+0800</dateCreated>
                    <form type="org.openmrs.Form">61f906c9-d81b-4f92-8739-5fece3826008</form>
                    <changedBy type="org.openmrs.User">eceaf821-4002-4d5d-884d-b8e7ee8be1b4</changedBy>
                    <patient type="org.openmrs.Patient">eb532006-b0dc-4210-9635-e8a18db16ebd</patient>
                    <voided type="boolean">false</voided>
                    <location type="org.openmrs.Location">15e3410c-ca6b-49fd-a090-140fe3d285d3</location>
                    <visit type="org.openmrs.Visit">304858d8-c0e6-4a85-a943-2765deb0f9ef</visit>
                    <encounterType type="org.openmrs.EncounterType">8d5b27bc-c2cc-11de-8d13-0010c6dffd0f</encounterType>
                  </org.openmrs.Encounter>
                </content>
              </SyncItem>
              <SyncItem containedType="org.openmrs.Obs" key="a9ab12d0-a5c2-483c-8fa6-727b70de853a" state="UPDATED">
                <content>
                  <org.openmrs.Obs>
                    <creator type="org.openmrs.User">eceaf821-4002-4d5d-884d-b8e7ee8be1b4</creator>
                    <voidReason type="string">htmlformentry</voidReason>
                    <concept type="org.openmrs.Concept">b16f9660-b32f-4fba-9b05-98bf375aa54f</concept>
                    <dateVoided type="timestamp">2022-02-25T14:20:01.270+0800</dateVoided>
                    <encounter type="org.openmrs.Encounter">d70a1e66-2b7d-4cff-a0c3-f272e73fee53</encounter>
                    <uuid type="string">a9ab12d0-a5c2-483c-8fa6-727b70de853a</uuid>
                    <voidedBy type="org.openmrs.User">eceaf821-4002-4d5d-884d-b8e7ee8be1b4</voidedBy>
                    <obsDatetime type="timestamp">2022-02-25T14:19:34.000+0800</obsDatetime>
                    <dateCreated type="timestamp">2022-02-25T14:19:34.000+0800</dateCreated>
                    <person type="org.openmrs.Person">eb532006-b0dc-4210-9635-e8a18db16ebd</person>
                    <voided type="boolean">true</voided>
                    <location type="org.openmrs.Location">15e3410c-ca6b-49fd-a090-140fe3d285d3</location>
                    <valueCoded type="org.openmrs.Concept">03d778a4-6f25-11e5-8a93-00163e005267</valueCoded>
                  </org.openmrs.Obs>
                </content>
              </SyncItem>
              <!-- 如果是想要更改这种一对多的关联关系，则使用PersistentSet -->
              <SyncItem containedType="org.hibernate.collection.internal.PersistentSet" key="d70a1e66-2b7d-4cff-a0c3-f272e73fee53|obs" state="UPDATED">
                <content>
                  <org.hibernate.collection.internal.PersistentSet>
                    <owner action="update" properyName="obs" type="org.openmrs.Encounter" uuid="d70a1e66-2b7d-4cff-a0c3-f272e73fee53"/>
                    <entry action="update" type="org.openmrs.Obs" uuid="3c8c10df-67e3-4947-911b-77482068c0cf"/>
                    <entry action="update" type="org.openmrs.Obs" uuid="a9ab12d0-a5c2-483c-8fa6-727b70de853a"/>
                    <entry action="update" type="org.openmrs.Obs" uuid="fb92fc8c-9b9d-4234-ada4-7c00cf5568e4"/>
                    <entry action="update" type="org.openmrs.Obs" uuid="24402884-31db-4bfc-9563-db3557307088"/>
                  </org.hibernate.collection.internal.PersistentSet>
                </content>
              </SyncItem>
            </items>
            ``
1, 下面谈谈对payload中xml的解析：
    （1）SyncItem类型为PersistentSet时：
        1，通过ownerUuid获取到对应的实体Encounter
            OpenmrsObject owner = (OpenmrsObject) SyncUtil.getOpenmrsObj(ownerClassName, ownerUuid);
        2，获取到关联的对象obs		
            String ownerCollectionPropertyName = ((Element) nodes.item(i)).getAttribute("properyName");
        3，通过invoke来获取到关联的obs
            Method m = SyncUtil.getGetterMethod(owner.getClass(), "obs");
            Set entries = (Set) m.invoke(owner, (Object[]) null);
        4，然后进行根据增删改的类型进行数据的更新
            SyncUtil.updateOpenmrsObject(owner, ownerClassName, ownerUuid);
2，生成payload中的xml，以visit为例
    1，获得visit所有声明的字段，即包括public、private和proteced，但是不包括父类的申明字段。
        // Transient properties are not serialized.
        Set<String>	transientProps = new HashSet<String>();
            for (Field f : entity.getClass().getDeclaredFields()) {
            if (Modifier.isTransient(f.getModifiers())) {
                transientProps.add(f.getName());
                if (log.isDebugEnabled())
                log.debug("The field " + f.getName() + " is transient - so we won't serialize it");
            }
        }
    2，通过SessionFactory得到ClassMetadata
        ClassMetadata data = getSessionFactory().getClassMetadata(entity.getClass());
        //如果有主键，
        if (data.hasIdentifierProperty()) {
            idPropertyName = data.getIdentifierPropertyName();
            idPropertyObj = ((org.hibernate.persister.entity.AbstractEntityPersister) data).getEntityMetamodel().getIdentifierProperty();
            if (id != null && idPropertyObj.getIdentifierGenerator() != null
                    && (idPropertyObj.getIdentifierGenerator() instanceof org.hibernate.id.Assigned
                    //	|| idPropertyObj.getIdentifierGenerator() instanceof org.openmrs.api.db.hibernate.NativeIfNotAssignedIdentityGenerator
                    )) {
                // serialize value as string
                values.put(idPropertyName, new PropertyClassValue(id.getClass().getName(), id.toString()));
            }
        } else if (data.getIdentifierType() instanceof EmbeddedComponentType) {
            // if we have a component identifier type (like AlertRecipient),
            // make
            // sure we include those properties
            EmbeddedComponentType type = (EmbeddedComponentType) data.getIdentifierType();
            for (int i = 0; i < type.getPropertyNames().length; i++) {
                String propertyName = type.getPropertyNames()[i];
                Object propertyValue = type.getPropertyValue(entity, i, EntityMode.POJO);
                addProperty(values, entity, type.getSubtypes()[i], propertyName, propertyValue, infoMsg);
            }
        }
数据发送的流程：
    1，使用ApplicationListener在程序启动的时候只执行一次，建立netty长连接，当然也可以通过手动的建立netty长连接
        @Component //用于项目启动时发现 也可以用@Service
        public class SearchReceive implements ApplicationListener<ContextRefreshedEvent> {
            @Override
            public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
                if (contextRefreshedEvent.getApplicationContext().getParent() == null) {//保证只执行一次
                    System.out.println("这里就执行了一次哦");
                }
            }
        }
    2，在HibernateSyncInterceptor中，生成SyncRecord后保存
    3，然后发送一个mq消息，在mq中使用netty的channel发送对应的SyncRecord
    3，Netty的Server端就收到SyncRecord后进行处理然后，返回OK
    4，客户端接收到ok之后更改SyncRecord的状态为complete





                        

                            
                            
        
        
        