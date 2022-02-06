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
                          	

                        

                            
                            
        
        
        