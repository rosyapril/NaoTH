#ifndef _CNN_rc17_augmented_7_H_
#define _CNN_rc17_augmented_7_H_

#include <limits>

#include <fstream>

#include "AbstractCNNClassifier.h"

class CNN_rc17_augmented_7 : public AbstractCNNClassifier {


public:

	bool classify(const BallCandidates::Patch& p);

  virtual float getBallConfidence();
  virtual float getNoballConfidence();

private:

	// declare copied intial input array
	float out_step0[16][16][1];

	// declare output for this image input step
	float out_step1[16][16][1];

	// declare output for this convolution step
	float out_step2[8][8][8];

	// declare output for this ReLU step
	float out_step3[8][8][8];

	// declare output for this max polling step
	float out_step4[4][4][8];

	// declare output for this convolution step
	float out_step5[4][4][10];

	// declare output for this ReLU step
	float out_step6[4][4][10];

	// declare output for this max polling step
	float out_step7[3][3][10];

	// declare output for this fully connected step
	float out_step8[64][1][1];

	// declare output for this ReLU step
	float out_step9[64][1][1];

	// declare output for this fully connected step
	float out_step10[2][1][1];

	// declare output for this SoftMax step
	float out_step11[2][1][1];

};

#endif

