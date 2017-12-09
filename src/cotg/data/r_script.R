
files_dir = list.files(pattern="militaryData.csv", full.names=T, recursive = FALSE)[1]

df = read.delim(files_dir, ",", header = TRUE)
x = df[df$alliance == "Dirty Mastiff Cartel",]
barplot(data.frame(x[, c(1,2)])[,1], names.arg = x[,2])


allianceNames = unique(df$alliance);

totalMilitaryPerAllianceDF = function(name) {
  return(df[df$alliance == name, c(1,2)])
}

totalMilitaryPerAlliancePlot = function(name) {
  var <- totalMilitaryPerAllianceDF(name)
  barplot(var, names.arg[var,2])
}

