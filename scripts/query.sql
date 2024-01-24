SELECT [ExecutionPackageLogDetails].[ExecutionPackageLogDetailId] as PackageLogDetailId,
    [ExecutionPackageLogDetails].[Name] as PackageLogDetailName,
    [ExecutionPackageLogDetails].[ObjectName] as PackageLogDetailObjectName,
    CAST([ExecutionPackageLogDetails].[Start] AS DATETIME2) as PackageLogDetailStart,
    CAST([ExecutionPackageLogDetails].[End] AS DATETIME2) as PackageLogDetailEnd,
    [ExecutionPackageLogDetails].[EndStatus] as PackageLogDetailEndStatus,
    [ExecutionPackageLogs].[ExecutionId] as PackageLogId,
    [ExecutionPackageLogs].[ExecutionPackageName] as PackageLogName,
    CAST([ExecutionPackageLogs].[Start] AS DATETIME2) as PackageLogStart,
    CAST([ExecutionPackageLogs].[End] AS DATETIME2) as PackageLogEnd,
    [ExecutionPackageLogs].[EndStatus] as PackageLogEndStatus,
    [ExecutionPackages].[ExecutionPackageId] as PackageId,
    [ExecutionPackages].[Name] as PackageName,
    [Projects].[ProjectId] as ProjectId,
    [Projects].[Name] as ProjectName
FROM [TX_REPOSITORY].[dbo].[ExecutionPackageLogs]
    INNER JOIN [TX_REPOSITORY].[dbo].[ExecutionPackages] ON [ExecutionPackages].[ExecutionPackageId] = [ExecutionPackageLogs].[ExecutionPackageId]
    INNER JOIN [TX_REPOSITORY].[dbo].[ExecutionPackageLogDetails] ON [ExecutionPackageLogDetails].[ExecutionId] = [ExecutionPackageLogs].[ExecutionId]
    INNER JOIN [TX_REPOSITORY].[dbo].[Projects] ON [Projects].[ProjectId] = [ExecutionPackages].[ProjectId]
WHERE [Projects].[IsDeployedVersion] = 1
    AND [Projects].[ValidTo] = 99999999
    AND [ExecutionPackages].ValidTo = 99999999
    AND [ExecutionPackageLogs].ExecutionPackageId in (
        SELECT DISTINCT [ExecutionPackages].[ExecutionPackageId]
        FROM [TX_REPOSITORY].[dbo].[ExecutionPackages]
            INNER JOIN [TX_REPOSITORY].[dbo].[Projects] ON [ExecutionPackages].ProjectId = [Projects].ProjectId
        where [Projects].IsDeployedVersion = 1
            and [Projects].ValidTo = 99999999
            and [ExecutionPackages].ValidTo = 99999999
    )
    AND [ExecutionPackageLogs].[Start] > '2024-01-24 00:00:00.000'
    AND [ExecutionPackageLogDetails].[End] is NOT null
    AND [ExecutionPackageLogs].[End] is NOT null
    AND [ExecutionPackageLogDetails].[ExecutionPackageLogDetailId] > 0
ORDER BY PackageLogDetailId;